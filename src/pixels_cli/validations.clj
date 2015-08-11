(ns pixels-cli.validations)

(defn validate-image-defined [{m :m n :n} command]
  (when (and (or (nil? m) (nil? n))
             (not (#{:new-image :exit} (:instruction command))))
    {:error "image not defined"}))

(defn validate-pixel-colours [image command]
  (when (and (get-in command [:input :colour])
             (not (re-find #"[A-Z]" (get-in command [:input :colour]))))
    {:error "colour must be a capital letter"}))

(defn within-limits? [m n [x y]]
  (and (>= y 1) (<= y n) (>= x 1) (<= m)))

(defn validate-pixel-coordinates [current-image translated-command]
  (let [{m :m n :n} current-image]
    (when (and
           (not= (:instruction translated-command) :new-image)
           (not (every? (fn [[[x y] v]] (within-limits? m n [x y]))
                        (-> translated-command :output :pixels))))
      {:error "some pixels are not withing the image definition"})))

(defn before-command-translation [app-state command]
  (if-let [error (some (fn [f] (f (:image @app-state) command))
                       [validate-image-defined
                        validate-pixel-colours])]
    error
    command))

(defn after-command-translation [app-state translated-command]
  (if-let [error (some (fn [f] (f (:image @app-state) translated-command))
                       [validate-pixel-coordinates])]
    error
    translated-command))
