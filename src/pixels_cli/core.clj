(ns pixels-cli.core
  (:require [clojure.string :as string]))

(defn parse-command [str]
  (let [chars (string/split str #" ")
        [char1 char2 char3 char4] chars]
    (case char1
      "X" {:command :exit}
      "S" {:command :show-image}
      "I" {:command :new-image
           :input {:m (Integer/parseInt char2)
                   :n (Integer/parseInt char3)}}
      "L" {:command :colour-pixel
           :input {:x (Integer/parseInt char2)
                   :y (Integer/parseInt char3)
                   :colour char4}}
      nil)))

(defn new-image [command]
  (let [m (get-in command [:input :m])
        n (get-in command [:input :n])
        pixels (into (sorted-map)
                     (zipmap (for [x (range m) y (range n)]
                               [(+ x 1) (+ y 1)])
                             (repeat "O")))]
    {:m m :n n :pixels pixels}))

(defn colour-pixel [command image]
  (let [{{x :x y :y colour :colour} :input} command]
    (assoc-in image [:pixels [x y]] colour)))

(defn render-image [{pixels :pixels m :m n :n}]
  (println (string/join "\n"
                        (map #(apply str (vals %1))
                             (partition-all m pixels)))))

(defn build-image [commands]
  (println "commands: " commands)
  (reduce (fn [image command]
            (case (:command command)
              :new-image (new-image command)
              :colour-pixel (colour-pixel command image)))
          {}
          commands))

(defn show-image [command app-state]
  (render-image (build-image (:history app-state)))
  app-state)

(defn terminate-session []
  (println "Terminating session. Bye"))

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
