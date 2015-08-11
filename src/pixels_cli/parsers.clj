(ns pixels-cli.parsers
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

(defn parse-horizontal-segment-command [[x1char x2char ychar colour]]
  {:instruction :horizontal-segment
   :input {:x1 (Integer/parseInt x1char)
           :x2 (Integer/parseInt x2char)
           :y (Integer/parseInt ychar)
           :colour colour}})

(defn parse-region-segment-command [[xchar ychar colour]]
  {:instruction :region-segment
   :input {:x (Integer/parseInt xchar)
           :y (Integer/parseInt ychar)
           :colour colour}})

(defn parse-command [str]
  (let [chars (string/split str #" ")]
    (try
      (case (first chars)
        "X" {:instruction :exit}
        "S" {:instruction :show-image}
        "C" {:instruction :clear-image}
        "I" (parse-new-image-command (rest chars))
        "L" (parse-colour-pixel-command (rest chars))
        "V" (parse-vertical-segment-command (rest chars))
        "H" (parse-horizontal-segment-command (rest chars))
        "F" (parse-region-segment-command (rest chars))
        {:error "not a valid command"})
      (catch NumberFormatException e
        {:error "not a valid argument"}))))
