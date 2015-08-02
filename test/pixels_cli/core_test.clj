(ns pixels-cli.core-test
  (:require [clojure.test :refer :all]
            [pixels-cli.core :refer :all]))

(deftest parse-commands
  (testing "exit command"
    (is (= {:command :exit} (parse-command "X"))))
  (testing "new image command"
    (is (= {:command :new-image
            :input {:M 5 :N 4}}
           (parse-command "I 5 4"))))
  (testing "show current image"
    (is (= {:command :show-image} (parse-command "S")))))
