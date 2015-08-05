(ns pixels-cli.core
  (:require [clojure.string :as string]))

(defn parse-command [str]
  (let [chars (string/split str #" ")]
    (case (first chars)
      "X" {:command :exit}
      "S" {:command :show-image}
      "I" {:command :new-image :input {:m (Integer/parseInt (second chars))
                                       :n (Integer/parseInt (last chars))}}
      nil)))

(defn terminate-session []
  (println "Terminating session. Bye"))

(defn new-image [command _]
  {:m (get-in command [:input :m])
   :n (get-in command [:input :n])})

(defn render-image [image]
  (let [m (:m image 0)
        n (:n image 0)
        coloured (:coloured image)]
    (dotimes [y n]
      (dotimes [x m]
        (print "O"))
      (println ""))))

(defn build-image [commands]
  (println "commands: " commands)
  (reduce (fn [image command]
            (case (:command command)
              :new-image (new-image command image)))
          {}
          commands))

(defn process-command [command app-state]
  (let [instruction (:command command)
        history (:history app-state)]
    (case instruction
      :new-image  (assoc app-state :history (conj history command))
      :show-image (render-image (build-image history))
      app-state)))

(defn -main [& args]
  (println "Tiny Interactive Graphical Editor")
  (println "Enter the commands, one command per line:")
  (loop [command (parse-command (read-line))
         app-state {:history []}]
    (println "parsed: " command)
    (if-not (= :exit (:command command))
      (let [new-app-state (process-command command app-state)]
        (recur (parse-command (read-line)) new-app-state)))))

