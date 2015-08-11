(ns pixels-cli.translators
  (:require [clojure.set :as set]))

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

(defn horizontal-segment [command]
  (let [{{x1 :x1 x2 :x2 y :y colour :colour} :input} command
        segment (for [x (range x1 (+ x2 1))] [[x y] colour])]
    {:pixels (into {} segment)}))

(defn adjacent-pixels [[x y]]
  (into #{} (for [sumx #{-1 1 0} sumy #{-1 1 0}
                  :when (not (and (= sumx 0) (= sumy 0)))]
              [(+ x sumx) (+ y sumy)])))

(defn within-limits? [m n [x y]]
  (and (>= y 1) (<= y n) (>= x 1) (<= m)))

(defn adjacent-with-same-colour
  [{current-image :pixels m :m n :n} pixels-to-check colour]
  (loop [pixels pixels-to-check
         region (into #{} pixels-to-check)]
    (let [adjacent-with-same-colour (into #{} (for [pixel pixels
                                                    adjacent (adjacent-pixels pixel)
                                                    :when (within-limits? m n adjacent)
                                                    :when (= (get current-image adjacent) colour)]
                                                adjacent))
          new-adjacent (set/difference adjacent-with-same-colour region)]
      (if (empty? new-adjacent)
        region
        (recur new-adjacent (into region new-adjacent))))))

(defn region-segment [{{x :x y :y new-colour :colour} :input} current-image]
  (let [current-colour (get-in current-image [:pixels [x y]])]
    {:pixels (zipmap (adjacent-with-same-colour current-image [[x y]] current-colour)
                     (repeat new-colour))}))

(defn clear-image [command {m :m n :n}]
  (new-image (assoc command :input {:m m :n n})))

(defn translate-command-to-pixels [app-state command]
  (let [translation (case (:instruction command)
                      :new-image (new-image command)
                      :clear-image (clear-image command (:image @app-state))
                      :colour-pixel (colour-pixel command)
                      :vertical-segment (vertical-segment command)
                      :horizontal-segment (horizontal-segment command)
                      :region-segment (region-segment command (:image @app-state))
                      :show-image {}
                      :exit {})]
    (assoc command :output translation)))
