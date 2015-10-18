(ns hiccup.table)

(defn- extract-attr [attr-blob label-key value]
  (cond (fn? attr-blob) (attr-blob label-key value)
        :true ;;(map? attr-blob)
        attr-blob))

(defn- if-nnil-apply [f value]
  (if (nil? f)
    value
    (f value)))

(defn- render-row [cell-type x-labels tr-attrs cell-attr transform-value-fn row]
  [:tr
   tr-attrs
   (map (fn [[label-key label]]
          (let [value (row label-key)]
            [cell-type
             (extract-attr cell-attr label-key value)
             (if-nnil-apply transform-value-fn value)]))
        x-labels)])

(defn to-table1d
  "Generates a hiccup tag structure of a table with the header row and the 'data'.
   The header row is defined by the order set in x-labels with the label.
   Optionally you may add a map of functions to add attribute to the respective tags.

  => (hiccup.table/to-table1d (list [21 \"John\" 180]
                                    [22 \"Wilfred\" 182]
                                    [23 \"Jack\" 179]
                                    [24 \"Daniel\" 165])
                              [1 \"Name\" 0 \"Age\" 2 \"Height\"])

OR
  => (hiccup.table/to-table1d (list {:age 21 :name \"John\" :height 180}
                                    {:age 22 :name \"Wilfred\" :height 182}
                                    {:age 23 :name \"Jack\" :height 179}
                                    {:age 24 :name \"Daniel\" :height 165})
                              [:name \"Name\" :age \"Age\" :height \"Height\"]) 

  ;=> [:table nil
        [:thead nil
         ([:th nil \"Name\"] [:th nil \"Age\"] [:th nil \"Height\"])]
        [:tbody nil
         ([:tr nil ([:td nil \"John\"] [:td nil 21] [:td nil 180])]
          [:tr nil ([:td nil \"Wilfred\"] [:td nil 22] [:td nil 182])]
          [:tr nil ([:td nil \"Jack\"] [:td nil 23] [:td nil 179])]
          [:tr nil ([:td nil \"Daniel\"] [:td nil 24] [:td nil 165])])]]

  => (let [attrs-fns {:table-attrs {:class \"mytable\"}
                      :thead-attrs {:id \"mythead\"}
                      :tbody-attrs {:id \"mytbody\"}
                      :th-fn (fn [label-key] {:class (subs (str label-key) 1)})
                      :tr-attrs {:class \"trattrs\"}
                      :td-fn (fn [label-key val]
                               (case label-key
                                 :height (if (<= 180 val)
                                           {:class \"above-avg\"}
                                           {:class \"below-avg\"}) nil))
                      :val-fn (fn [label-key val]
                                (if (= :name label-key)
                                  [:a {:href (str \"/\" val)} val]
                                  val))}]
       (hiccup.table/to-table1d (list {:age 21 :name \"John\" :height 180}
                                      {:age 22 :name \"Wilfred\" :height 182})
                                [:name \"Name\" :age \"Age\" :height \"Height\"] attrs-fns))
; => [:table {:class \"mytable\"}
     [:thead {:id \"mythead\"}
      ([:th {:class \"name\"} \"Name\"] [:th {:class \"age\"} \"Age\"] [:th {:class \"height\"} \"Height\"])]
     [:tbody {:id \"mytbody\"}
      ([:tr {:class \"trattrs\"} ([:td nil [:a {:href \"/John\"} \"John\"]] [:td nil 21] [:td {:class \"above-avg\"} 180])]
       [:tr {:class \"trattrs\"} ([:td nil [:a {:href \"/Wilfred\"} \"Wilfred\"]] [:td nil 22] [:td {:class \"above-avg\"} 182])])]]"
  ([data x-labels]
     (to-table1d data x-labels nil))
  ([data x-labels {:keys [table-attrs
                          thead-attrs
                          tbody-attrs
                          th-attrs
                          data-tr-attrs
                          data-td-attrs
                          data-value-transform]}]
     {:pre [(every? (some-fn nil? map? fn?)
                    [table-attrs thead-attrs tbody-attrs th-attrs data-tr-attrs data-td-attrs data-value-transform])]}
     (let [x-labels (if (vector? x-labels)
                      (partition 2 x-labels)
                      x-labels)]
       [:table
        table-attrs
        [:thead
         thead-attrs
         (render-row :th
                     x-labels
                     nil
                     th-attrs
                     nil
                     (into {} (map vec x-labels)))]
        [:tbody
         tbody-attrs
         (map (partial render-row :td x-labels data-tr-attrs data-td-attrs data-value-transform)
              data)]])))
