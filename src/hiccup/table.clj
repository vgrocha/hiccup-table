(ns hiccup.table)

(defn to-table1d
  ([data x-labels]
     (to-table1d data x-labels nil))
  ([data x-labels {:keys [table-attrs
                          thead-attrs
                          tbody-attrs
                          th-fn
                          tr-attrs
                          td-fn
                          val-fn]}]
     {:pre [(every? (some-fn nil? map? fn?)
                    [table-attrs thead-attrs tbody-attrs th-fn tr-attrs td-fn val-fn])]}
     (let [x-labels (if (vector? x-labels)
                      (partition 2 x-labels)
                      x-labels)]
       [:table
        (when (map? table-attrs) table-attrs)
        [:thead
         (when (map? thead-attrs) thead-attrs)
         (for [[label-key label] (seq x-labels)]
           [:th
            (cond (map? th-fn) th-fn
                  (fn? th-fn) (th-fn label-key))
            label])]
        [:tbody
         (when (map? tbody-attrs) tbody-attrs)
         (for [row data]
           [:tr
            (when (map? tr-attrs) tr-attrs)
            (for [[label-key label] x-labels]
              (let [val (row label-key)]
                [:td
                 (cond (map? td-fn) td-fn
                       (fn? td-fn) (td-fn label-key val))
                 (if (fn? val-fn)
                   (val-fn label-key val)
                   val)]))])]])))
