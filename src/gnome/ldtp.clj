(ns gnome.ldtp
  (:require [clojure.contrib.logging :as log])
  (:import [org.apache.xmlrpc.client XmlRpcClient XmlRpcClientConfigImpl]
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

(def client 
  (let [config (XmlRpcClientConfigImpl.)
        xclient (XmlRpcClient.)]
    (comment (.setServerURL config (java.net.URL. url)))
    (.setConfig xclient config)
    xclient))

(defn- xmlrpcmethod-arity "Generate code for one arity of xmlrpc method."
  [fnname argsyms n]
  (let [theseargs (take n argsyms)]
    `(~(vec theseargs)
     (clojure.lang.Reflector/invokeInstanceMethod
      client "execute" (to-array (list ~fnname ~(concat '(list) theseargs)))))) )

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

(defn set-url [url]
  (.setServerURL (.getConfig client) (java.net.URL. url)))

;;Generate all LDTP functions from the specfile.  Specfile is produced
;;by this python script:
;;https://github.com/weissjeffm/ldtp-server/blob/master/extract-api.py
;;see resources/prettify.clj to save in readable format
(defxmlrpc "ldtp_api.clj")

(defn action [uifn arg1 & args]
  (let [ids (if (satisfies? LDTPLocatable arg1) (locator arg1) (list arg1))]
     (log/info (str "Action: " (:name (meta uifn)) ids " " args))
     (apply uifn (concat ids args))))
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

