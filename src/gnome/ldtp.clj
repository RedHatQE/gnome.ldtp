(ns gnome.ldtp
  (:require [clojure.tools.logging :as log]
            [necessary-evil.core :as xml-rpc])
  (:import
   ;[org.apache.xmlrpc.client XmlRpcClient XmlRpcClientConfigImpl]
   [java.io PushbackReader InputStreamReader]))

(defprotocol LDTPLocatable
  (locator [x]))

(defrecord Window [id] LDTPLocatable
  (locator [this] [id]))

(defrecord Element [window id] LDTPLocatable
  (locator [this] (conj (locator window) id)))

(defrecord TabGroup [window id] LDTPLocatable
  (locator [this] (conj (locator window) id)))

(defrecord Tab [tabgroup id] LDTPLocatable
  (locator [this] (conj (locator tabgroup) id)))

(comment ;old way using apache xmlrpc client
  (def client 
    (let [config (XmlRpcClientConfigImpl.)
          xclient (XmlRpcClient.)]
      (comment (.setServerURL config (java.net.URL. url)))
      (.setEnabledForExtensions config true)
      (.setConfig xclient config)
      xclient)))

(def client (atom nil))

(defn- xmlrpcmethod-arity "Generate code for one arity of xmlrpc method."
  [fnname argsyms n]
  (let [theseargs (take n argsyms)]
    `(~(vec theseargs)
      (xml-rpc/call @client ~fnname ~@theseargs))))

(defmacro defxmlrpc
  "Generate functions corresponding to an xmlrpc API (in this case,
    LDTP). Generates arities for xmlrpc methods that have optional
    arguments. Reads a given spec file that lists the method name,
    arguments, and number of default args. See also
    http://ldtp.freedesktop.org/user-doc/index.html"
  [specfile]
  (let [methods (-> (ClassLoader/getSystemClassLoader)
                    (.getResourceAsStream specfile)
                    InputStreamReader. PushbackReader. read)
        defs (map
              (fn [[fnname [args num-optional-args]]]
                (let [argsyms (map symbol args)
                      num-required-args (- (count args) num-optional-args)
                      arity-arg-counts (range num-required-args (inc (count args)))
                      arities (for [arity arity-arg-counts] (xmlrpcmethod-arity fnname argsyms arity))]
                  
		    `(defn ~(symbol fnname)
		        ~@arities
		       )))
		methods)]
      `(do
         ~@defs)))

(comment ;old way using apache xmlrpc client
  (defn set-url [url]
    (.setServerURL (.getConfig client) (java.net.URL. url))))

(defn set-url [url] (reset! client url))

;;Generate all LDTP functions from the specfile.  Specfile is produced
;;by this python script:
;;https://github.com/weissjeffm/ldtp-server/blob/master/extract-api.py
;;see resources/prettify.clj to save in readable format
(defxmlrpc "ldtp_api.clj")

(defn get-fn-name [func] (second (clojure.string/split (str func) #"\$|@")))

(defn action [uifn & args]
  (let [arg1 (first args)
        ids (if (satisfies? LDTPLocatable arg1)
              (locator arg1)
              (if (nil? arg1) '() (list arg1)))
        result (atom "Error")]
    (try
      (reset! result (apply uifn (concat ids (rest args))))
      (finally
       (log/info (str "Action: " (get-fn-name uifn) " " args ", Result: "
                      @result))))))


;; Some higher level convenience functions that aren't supplied directly by ldtp

(defn- waittillwindow [windowid seconds exist?]
  (apply (if exist? waittillguiexist waittillguinotexist) (list windowid "" seconds)))

(defn waittillwindowexist [windowid seconds]
  (waittillwindow windowid seconds true))

(defn waittillwindownotexist [windowid seconds]
  (waittillwindow windowid seconds false))

(defn- bool [i]
  (= 1 i))

(defn exists? [windowid objectid]
  (bool (objectexist windowid objectid)))

(defn showing? [windowid objectid]
  (bool (hasstate windowid objectid "SHOWING")))

(defn rowexist? [windowid objectid row]
  (bool (doesrowexist windowid objectid row)))

(defmacro loop-with-timeout [timeout bindings & forms]
  `(let [starttime# (System/currentTimeMillis)]
     (loop ~bindings
       (if  (> (- (System/currentTimeMillis) starttime#) ~timeout)
	 0
	 (do ~@forms)))))

(defn waittillshowing [windowid objectid s]
  (loop-with-timeout (* s 1000) []
     (if-not (showing? windowid objectid)
           (do (Thread/sleep 500)
               (recur))
           1)))
           
(defn waittillnotshowing [windowid objectid s]
  (loop-with-timeout (* s 1000) []
     (if (showing? windowid objectid)
           (do (Thread/sleep 500)
               (recur))
           1)))
