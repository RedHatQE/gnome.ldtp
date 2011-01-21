(with-open [f (FileWriter. "/home/jweiss/workspace/gnome.ldtp/resources/ldtp_api_pretty.clj")]
  (pprint (with-in-str
            (slurp "/home/jweiss/workspace/gnome.ldtp/resources/ldtp_api.clj") (read))
          f))
