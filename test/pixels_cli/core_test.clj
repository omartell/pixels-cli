(ns pixels-cli.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [pixels-cli.core :refer :all]))

(defn image [& strings]
  (string/join (map #(str %1 "\n") strings)))

(deftest processing-commands
  (testing "new image"
    (is (= {:history [{:command :new-image}]}
           (process-command {:command :new-image}
                            {:history []}))))
  (testing "not recognised command"
    (is (= {:history []}
           (process-command {:command :launch-rocket}
                            {:history []})))))

(deftest building-images
  (is (= {:m 4 :n 3}
         (build-image [{:command :new-image
                        :input {:m 4 :n 3}}]))))

(deftest rendering-images
  (is (= (image "OOOO"
                "OOOO"
                "OOOO")
         (with-out-str (render-image {:m 4
                                      :n 3
                                      :coloured {}})))))

(deftest parsing-commands
  (testing "exit command"
    (is (= {:command :exit} (parse-command "X"))))
  (testing "new image command"
    (is (= {:command :new-image
            :input {:m 5 :n 4}}
           (parse-command "I 5 4"))))
  (testing "show current image"
    (is (= {:command :show-image} (parse-command "S"))))
  (testing "colour pixel X Y"
    (is (= {:command :colour-pixel :input {:x 1 :y 1 :colour "C"}}
           (parse-command "L 1 1 C")))))
