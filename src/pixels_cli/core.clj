(ns pixels-cli.core
  (:require [clojure.string :as string]))

(defn parse-command [str]
  (let [chars (string/split str #" ")]
    (case (first chars)
      "X" {:command :exit}
      "S" {:command :show-image}
      "I" {:command :new-image :input {:m (Integer/parseInt (second chars))
                                       :n (Integer/parseInt (last chars))}}
      "L" {:command :colour-pixel :input {:x (Integer/parseInt (second chars))
                                          :y (Integer/parseInt (chars 2))
                                          :colour (last chars)}}
      nil)))

(defn terminate-session []
  (println "Terminating session. Bye"))

(defn new-image [command _]
  {:m (get-in command [:input :m])
   :n (get-in command [:input :n])})

(defn colour-pixel [command image]
  (update-in image
             [:coloured]
             (fn [m] (let [x (get-in command [:input :x])
                          y (get-in command [:input :y])
                          colour (get-in command [:input :colour])]
                      (assoc m [x y] colour)))))

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
              :new-image (new-image command image)
              :colour-pixel (colour-pixel command image)))
          {}
          commands))

(defn show-image [command app-state]
  (render-image (build-image (:history app-state)))
  app-state)

(defn process-command [command app-state]
  (let [instruction (:command command)]
    (condp some [instruction]
      #{:new-image :colour-pixel} (update-in app-state [:history] conj command)
      #{:show-image} (show-image command app-state)
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
