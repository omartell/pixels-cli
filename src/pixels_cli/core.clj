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

(defn colour-pixel [command]
  (let [{{x :x y :y colour :colour} :input} command]
    {:pixels {[x y] colour}}))

(defn vertical-segment [command]
  (let [{{x :x y1 :y1 y2 :y2 colour :colour} :input} command
        segment (for [y (range y1 (+ y2 1))] [[x y] colour])]
    {:pixels (into {} segment)}))

(defn show-image [{pixels :pixels m :m n :n}]
  (let [coordinates-by-y-axis (into (sorted-map-by (fn [[x1 y1] [x2 y2]]
                                                     (compare [y1 x1] [y2 x2])))
                                    pixels)]
    (println "Current image:")
    (println (string/join "\n"
                          (map #(apply str (vals %1))
                               (partition-all m coordinates-by-y-axis))))))

(defn translate-command-to-pixels [command]
  (let [translation (case (:instruction command)
                      :new-image (new-image command)
                      :colour-pixel (colour-pixel command)
                      :vertical-segment (vertical-segment command)
                      :show-image {}
                      :exit {})]
    (assoc command :output translation)))

(defn terminate-session []
  (println "Terminating session. Bye"))

(defn validate-image-defined [{m :m n :n} command]
  (when (and (or (nil? m) (nil? n))
             (not (#{:new-image :exit} (:instruction command))))
    {:error "image not defined"}))

(defn validate-pixel-colours [image command]
  (when (and (get-in command [:input :colour])
             (not (re-find #"[A-Z]" (get-in command [:input :colour]))))
    {:error "colour must be a capital letter"}))

(defn validate-pixel-coordinates [current-image translated-command]
  (let [{m :m n :n} current-image]
    (when (and
           (not= (:instruction translated-command) :new-image)
           (not (every? (fn [[[x y] v]] (and (>= y 1) (<= y n)
                                            (>= x 1) (<= m)))
                        (-> translated-command :output :pixels))))
      {:error "some pixels are not withing the image definition"})))

(defn validations-before-translation [app-state command]
  (if-let [error (some (fn [f] (f (:image @app-state) command))
                       [validate-image-defined
                        validate-pixel-colours])]
    error
    command))

(defn validations-after-translation [app-state translated-command]
  (if-let [error (some (fn [f] (f (:image @app-state) translated-command))
                       [validate-pixel-coordinates])]
    error
    translated-command))

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
      (let [parsed-command (parse-command str-command)]
        (->> parsed-command
             (apply-or-error (partial validations-before-translation app-state))
             (apply-or-error translate-command-to-pixels)
             (apply-or-error (partial validations-after-translation app-state))
             (apply-or-error (partial process-command app-state))
             (handle-errors))
        (when-not (= :exit (:instruction parsed-command))
          (recur (read-line)))))))
