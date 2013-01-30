
;Assuming ldtp_api_raw.clj is the unformatted file
;this will spit the formatted version in ldtp_api_pretty.clj.
;Note that ldtp_api.clj is the git controlled (and hopefully
;latest) version of ldtp_api_pretty.clj.

(use 'clojure.java.io)
(use 'clojure.pprint)
(with-open [f (writer "/home/jmolet/Projects/gnome.ldtp/resources/ldtp_api_pretty.clj")]
  (pprint (with-in-str
            (slurp "/home/jmolet/Projects/gnome.ldtp/resources/ldtp_api_raw.clj") (read))
          f))
