(ns pixels-cli.core
  (:require [clojure.string :as string]))

(defn parse-new-image-command [[mchar nchar]]
  (let [m (Integer/parseInt mchar)
        n (Integer/parseInt nchar)]
    (if (< n 250)
      {:command :new-image :input {:m m :n n}}
      {:command :new-image :error "n must be a number <= 250"})))

(defn parse-command [str]
  (let [chars (string/split str #" ")
        [char1 char2 char3 char4 char5] chars]
    (case char1
      "X" {:command :exit}
      "S" {:command :show-image}
      "I" (parse-new-image-command (rest chars))
      "L" {:command :colour-pixel
           :input {:x (Integer/parseInt char2)
                   :y (Integer/parseInt char3)
                   :colour char4}}
      "V" {:command :vertical-segment
           :input {:x (Integer/parseInt char2)
                   :y1 (Integer/parseInt char3)
                   :y2 (Integer/parseInt char4)
                   :colour char5}}
      nil)))

(defn new-image [command]
  (let [column (get-in command [:input :m])
        row (get-in command [:input :n])
        pixels (zipmap (for [x (range column) y (range row)]
                         [(+ x 1) (+ y 1)])
                       (repeat "O"))]
    {:m column :n row :pixels pixels}))

(defn colour-pixel [command image]
  (let [{{x :x y :y colour :colour} :input} command]
    (assoc-in image [:pixels [x y]] colour)))

(defn vertical-segment [command image]
  (let [{{x :x y1 :y1 y2 :y2 colour :colour} :input} command
        segment (for [y (range y1 (+ y2 1))] [[x y] colour])]
    (update-in image [:pixels] #(into %1 segment))))

(defn render-image [{pixels :pixels m :m n :n}]
  (let [coordinates-by-y-axis (into (sorted-map-by (fn [[x1 y1] [x2 y2]]
                                                     (compare [y1 x1] [y2 x2])))
                                    pixels)]
    (println (string/join "\n"
                          (map #(apply str (vals %1))
                               (partition-all m coordinates-by-y-axis))))))

(defn build-image [commands]
  (println "commands: " commands)
  (reduce (fn [image command]
            (case (:command command)
              :new-image (new-image command)
              :colour-pixel (colour-pixel command image)
              :vertical-segment (vertical-segment command image)))
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
      #{:new-image
        :colour-pixel
        :vertical-segment} (update-in app-state [:history] conj command)
      #{:show-image} (show-image command app-state)
      app-state)))

(defn show-error [error]
  (println (str "Error: " error)))

(defn -main [& args]
  (println "Tiny Interactive Graphical Editor")
  (println "Enter the commands, one command per line:")
  (loop [command (parse-command (read-line))
         app-state {:history []}]
    (cond
      (contains? command :error)   (show-error (:error command))
      (= :exit (:command command)) (terminate-session)
      :else (let [new-app-state (process-command command app-state)]
              (recur (parse-command (read-line)) new-app-state)))))
