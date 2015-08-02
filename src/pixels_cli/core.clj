(ns pixels-cli.core
  (:require [clojure.string :as string]))

(defn parse-command [str]
  (let [chars (string/split str #" ")]
    (case (first chars)
      "X" {:command :exit}
      "S" {:command :show-image}
      "I" {:command :new-image :input {:M (Integer/parseInt (second chars))
                                       :N (Integer/parseInt (last chars))}}
      nil)))

(defn terminate-session []
  (println "Terminating session. Bye"))

(defn -main [& args]
  (println "Tiny Interactive Graphical Editor")
  (println "Enter the commands, one command per line:")
  (loop [input (read-line)]
    (if (= :exit (:command (parse-command input)))
      (terminate-session)
      (recur (read-line)))))
