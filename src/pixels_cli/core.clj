(ns pixels-cli.core
  (:require [clojure.string :as string]
            [pixels-cli.validations :as validations]
            [pixels-cli.translators :as translators]
            [pixels-cli.parsers :as parsers]))

(defn terminate-session []
  (println "Terminating session. Bye"))

(defn show-image [{pixels :pixels m :m n :n}]
  (let [coordinates-by-y-axis (into (sorted-map-by (fn [[x1 y1] [x2 y2]]
                                                     (compare [y1 x1] [y2 x2])))
                                    pixels)]
    (println "Current image:")
    (println (string/join "\n"
                          (map #(apply str (vals %1))
                               (partition-all m coordinates-by-y-axis))))))

(defn process-command [app-state translated-command]
  (let [image (:image @app-state) m (:m image) n (:n image)]
    (case (:instruction translated-command)
      :show-image (show-image image)
      :exit (terminate-session)
      (reset! app-state
              {:history (conj (:history @app-state) translated-command)
               :image (merge-with #(if (map? %1) (conj %1 %2) %2)
                                  image
                                  (:output translated-command))}))))

(defn handle-errors [m]
  (when (:error m)
    (println (str "Error: " (:error m)))))

(defn apply-or-error [f input]
  (if (:error input)
    input
    (f input)))

(defn -main [& args]
  (println "Tiny Interactive Graphical Editor")
  (println "Enter the commands, one command per line:")
  (let [app-state (atom {:history [] :image {} })]
    (loop [str-command (read-line)]
      (let [parsed-command (parsers/parse-command str-command)]
        (->> parsed-command
             (apply-or-error (partial validations/before-command-translation app-state))
             (apply-or-error (partial translators/translate-command-to-pixels app-state))
             (apply-or-error (partial validations/after-command-translation app-state))
             (apply-or-error (partial process-command app-state))
             (handle-errors))
        (when-not (= :exit (:instruction parsed-command))
          (recur (read-line)))))))
