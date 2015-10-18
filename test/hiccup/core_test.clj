(ns hiccup.core-test
  (:require [clojure.test :refer :all]
            [hiccup.table :refer :all]))

(deftest extract-attr-test
  (let [extract-attr #'hiccup.table/extract-attr]
    (is (= :asd
           (extract-attr (fn [a b] a) :asd 1)))
    (is (= 1
           (extract-attr (fn [a b] b) :asd 1)))
    (is (= {:class "asd-1"}
           (extract-attr (fn [key value]
                           {:class (clojure.string/join "-" [(name key) value])})
                         :asd
                         1)))))

(deftest render-row-test
  (let [render-row #'hiccup.table/render-row]
    (is (= [:tr nil
            '([:td nil "value 20"]
              [:td nil "value 15"]
              [:td nil "value 10"])]
           (render-row :td
             [[:a "A header"] [:b "B header"] [:c "C header"]]
             nil
             nil
             nil
             {:c "value 10" :b "value 15" :a "value 20"})))
    
    (is (= [:tr {:class "test-me"}
            '([:td {:class "a-value 20"} "VALUE 20"]
              [:td {:class "b-value 15"} "VALUE 15"]
              [:td {:class "c-value 10"} "VALUE 10"])]
           (#'hiccup.table/render-row :td
                                      [[:a "This is A"] [:b "This is B"] [:c "This is C"]]
                                      {:class "test-me"}
                                      (fn [label-key value]
                                        {:class (clojure.string/join "-" [(name label-key) value])})
                                      clojure.string/upper-case
                                      {:c "value 10" :b "value 15" :a "value 20"})))))

(deftest table1d-test
  (is (= (hiccup.table/to-table1d (list {:age 21 :name "John" :height 180}
                                        {:age 22 :name "Wilfred" :height 182}
                                        {:age 23 :name "Jack" :height 179}
                                        {:age 24 :name "Daniel" :height 165})
                                  [:name "Name" :age "Age" :height "Height"])
         
         (hiccup.table/to-table1d (list [21 "John" 180]
                                        [22 "Wilfred" 182]
                                        [23 "Jack" 179]
                                        [24 "Daniel" 165])
                                  [1 "Name" 0 "Age" 2 "Height"])
         
         '[:table nil
           [:thead nil
            [:tr nil
             ([:th nil "Name"] [:th nil "Age"] [:th nil "Height"])]]
           [:tbody nil
            ([:tr nil ([:td nil "John"] [:td nil 21] [:td nil 180])]
             [:tr nil ([:td nil "Wilfred"] [:td nil 22] [:td nil 182])]
             [:tr nil ([:td nil "Jack"] [:td nil 23] [:td nil 179])]
             [:tr nil ([:td nil "Daniel"] [:td nil 24] [:td nil 165])])]])))


(deftest table1d-attrs-test
  (let [common-attrs-fns {:table-attrs {:class "mytable"}
                          :thead-attrs {:id "mythead"}
                          :tbody-attrs {:id "mytbody"}
                          :data-tr-attrs {:class "trattrs"}
                          } ]
    (let [attrs-fns (into common-attrs-fns
                          {:th-attrs (fn [label-key _] {:class (name label-key)})
                           :data-td-attrs (fn [label-key val]
                                            (case label-key
                                              :height (if (<= 180 val)
                                                        {:class "above-avg"}
                                                        {:class "below-avg"}) nil))
                           :val-fn (fn [label-key val]
                                     (if (= :name label-key)
                                       [:a {:href (str "/" val)} val]
                                       val))})])
    
    (is (= (hiccup.table/to-table1d
            (list {:age 21 :name "John" :height 179}
                  {:age 22 :name "Wilfred" :height 182})
            [:name "Name" :age "Age" :height "Height"]
            attrs-fns)
           
           '[:table {:class "mytable"}
             [:thead {:id "mythead"}
              [:tr nil ([:th {:class "name"} "Name"]
                        [:th {:class "age"} "Age"]
                        [:th {:class "height"} "Height"])]]
             [:tbody {:id "mytbody"}
              ([:tr {:class "trattrs"} ([:td nil "John"] [:td nil 21] [:td {:class "below-avg"} 179])]
               [:tr {:class "trattrs"} ([:td nil "Wilfred"] [:td nil 22] [:td {:class "above-avg"} 182])])]]))))
