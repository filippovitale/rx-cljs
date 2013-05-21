(ns rx-cljs.observable-test
  ;; (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  ;; (:require [cemerick.cljs.test :as t]
  ;;           [rx-cljs.observable :as o])
  (:require-macros [error.macros :refer [test is]])
  (:require [error.test]
            [rx-cljs.observable :as o]))

(test "creating observables from single values"
      (-> (o/return-value 50)
          (o/subscribe (fn [v]
                         (is (= 50 v)))))

      (let [result (atom [])]
        (-> (o/from-array [4 6 8])
            (o/subscribe (fn [v]
                           (swap! result #(conj % v)))))
        (is (= [4 6 8] @result)))
      (done))

(test "taking"
      (let [result (atom [])]
        (-> (o/from-array [4 6 8])
            (o/take 2)
            (o/subscribe (fn [v]
                           (swap! result #(conj % v)))))
        (is (= [4 6] @result))
        (done)))

(test "mapping"
      (-> (o/return-value 50)
          (o/map (fn [v] (* 2 v)))
          (o/subscribe (fn [v]
                         (is (= 100 v))
                         (done)))))

(test "reducing"
      (-> (o/from-array [4 6 8])
          (o/map (fn [v] (* 2 v)))
          (o/reduce +)
          (o/subscribe (fn [v]
                         (is (= 36 v))
                         (done)))))

(test "composing"
      (let [project-range (fn [n]
                            (o/return-value (range n)))
            result (atom [])]

        (-> (o/from-array [4 6 8])
            (o/select-many project-range)
            (o/subscribe (fn [v]
                           (swap! result #(conj % v)))))
        (is (= ['(0 1 2 3) '(0 1 2 3 4 5) '(0 1 2 3 4 5 6 7)]
               @result))
        (done)))


(test "interval"
      (let [n 10
            result (atom [])]
        (-> (o/interval 1)
            (o/take (inc n))
            (o/subscribe (fn [v]
                           (if (< (count @result) n)
                             (swap! result #(conj % v))
                             (do (is (= [0 1 2 3 4 5 6 7 8 9] @result))
                                 (done))))))))  

(test "disposing subscriptions"
      (let [token (-> (o/interval 200)
                      (o/subscribe (fn [_] )))]
        (is (not (true? (.-isStopped token))))
        (o/dispose token)
        (is (true? (.-isStopped token)))
        (done)))
  
  
