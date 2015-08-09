(ns pixels-cli.core
  (:require [clojure.string :as string]))

(defn parse-new-image-command [[mchar nchar]]
  (let [m (Integer/parseInt mchar)
        n (Integer/parseInt nchar)]
    (if (< n 250)
      {:instruction :new-image :input {:m m :n n}}
      {:error "n must be a number <= 250"})))

(defn parse-colour-pixel-command [[xchar ychar colour]]
  {:instruction :colour-pixel
   :input {:x (Integer/parseInt xchar)
           :y (Integer/parseInt ychar)
           :colour colour}})

(defn parse-vertical-segment-command [[xchar y1char y2char colour]]
  {:instruction :vertical-segment
   :input {:x (Integer/parseInt xchar)
           :y1 (Integer/parseInt y1char)
           :y2 (Integer/parseInt y2char)
           :colour colour}})

(defn parse-command [str]
  (let [chars (string/split str #" ")]
    (try
      (case (first chars)
        "X" {:instruction :exit}
        "S" {:instruction :show-image}
        "I" (parse-new-image-command (rest chars))
        "L" (parse-colour-pixel-command (rest chars))
        "V" (parse-vertical-segment-command (rest chars))
        {:error "not a valid command"})
      (catch NumberFormatException e
        {:error "not a valid argument"}))))

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

(defn show-image [{pixels :pixels m :m n :n}]
  (let [coordinates-by-y-axis (into (sorted-map-by (fn [[x1 y1] [x2 y2]]
                                                     (compare [y1 x1] [y2 x2])))
                                    pixels)]
    (println "Current image:")
    (println (string/join "\n"
                          (map #(apply str (vals %1))
                               (partition-all m coordinates-by-y-axis))))))

(defn execute-command [command image]
  (case (:instruction command)
    :new-image (new-image command)
    :colour-pixel (colour-pixel command image)
    :vertical-segment (vertical-segment command image)))

(defn terminate-session []
  (println "Terminating session. Bye"))

(defn validate-image-defined [{m :m n :n} command]
  (when (and
         (or (nil? m) (nil? n))
         (not= (:instruction command) :new-image))
    {:error "image not defined"}))

(defn process-command [app-state command]
  (let [image (:image @app-state) m (:m image) n (:n image)]
    (case (:instruction command)
      :show-image (show-image image)
      :exit (terminate-session)
      (if-let [error (validate-image-defined image command)]
        error
        (reset! app-state
                {:history (conj (:history @app-state) command)
                 :image (execute-command command image)})))))

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
      (let [parsed-command (parse-command str-command)]
        (->> parsed-command
             (apply-or-error (partial process-command app-state))
             (handle-errors))
        (when-not (= :exit (:instruction parsed-command))
          (recur (read-line)))))))
