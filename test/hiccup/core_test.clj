(ns hiccup.core-test
  (:require [clojure.test :refer :all]
            [hiccup.table :refer :all]))

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
         
         '[:table nil [:thead nil ([:th nil "Name"] [:th nil "Age"] [:th nil "Height"])] [:tbody nil ([:tr nil ([:td nil "John"] [:td nil 21] [:td nil 180])] [:tr nil ([:td nil "Wilfred"] [:td nil 22] [:td nil 182])] [:tr nil ([:td nil "Jack"] [:td nil 23] [:td nil 179])] [:tr nil ([:td nil "Daniel"] [:td nil 24] [:td nil 165])])]]))
  )


(deftest table1d-attrs-test
  (let [attrs-fns {:table-attrs {:class "mytable"}
                   :thead-attrs {:id "mythead"}
                   :tbody-attrs {:id "mytbody"}
                   :th-fn (fn [label-key] {:class (subs (str label-key) 1)})
                   :tr-attrs {:class "trattrs"}
                   :td-fn (fn [label-key val]
                           (case label-key
                             :height (if (<= 180 val)
                                       {:class "above-avg"}
                                       {:class "below-avg"}) nil))
                   :val-fn (fn [label-key val]
                             (if (= :name label-key)
                               [:a {:href (str "/" val)} val]
                               val))}]
                               
    (is (= (hiccup.table/to-table1d (list {:age 21 :name "John" :height 180}
                                          {:age 22 :name "Wilfred" :height 182})
                                    [:name "Name" :age "Age" :height "Height"] attrs-fns))
        '[:table {:class "mytable"} [:thead {:id "mythead"} ([:th {:class "name"} "Name"] [:th {:class "age"} "Age"] [:th {:class "height"} "Height"])] [:tbody {:id "mytbody"} ([:tr {:class "trattrs"} ([:td nil [:a {:href "/John"} "John"]] [:td nil 21] [:td {:class "above-avg"} 180])] [:tr {:class "trattrs"} ([:td nil [:a {:href "/Wilfred"} "Wilfred"]] [:td nil 22] [:td {:class "above-avg"} 182])])]])))
