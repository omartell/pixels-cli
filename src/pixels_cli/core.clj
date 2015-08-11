(ns pixels-cli.core
  (:require [clojure.string :as string]
            [pixels-cli.validations :as validations]
            [pixels-cli.translators :as translators]
            [pixels-cli.parsers :as parsers]))

(defn terminate-session []
  (println "Terminating session. Bye")
  (System/exit 0))

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

(defn -main
  "This function is in charge of setting up the application state and the flow of
  the program. The application state is an atom wrapping a map with an image key
  and a history key.

  The image key is our image representation at a specific point in time and it's
  also a map having pixel coordinates ([x y]) as keys and colours ('C') as values.
  The history key is just the list of commands processesed and it's only used for
  debugging purposes right now.

  The program runs in a loop waiting for lines to be read from STDIN, then takes
  each line and runs it through a pipeline of functions. These functions at the
  end generate a new image representation, which is then persisted into the
  application state if everything worked correctly.

  There is also some basic validation performed to check things like pixels
  represented with capital letters and within image bounds. These validations
  could result into errors, currently represented with maps, which are skippped
  by any subsequent steps and processed at the end to show the error message to
  the user."
  [& args]
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
