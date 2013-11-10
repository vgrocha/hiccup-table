(ns hiccup.table
  
)



#_(defn to-table
  "Given an incanter dataset, returns an html table.

   Optionally, new columns names can be set (e.g if they are keywords)
   with a 'labels' map, e.g.

      (html-table<- 'ds' {:col1 \"Good looking name for col 1\"
                          :col2 \"Good looking name for col 2\"})

   The optional map contains functions or map for the respective attributes
   of the tags:

       * 'table-fn', 'thead-fn', tbody-fn are map only, e.g.:
         => (html-table<- 'ds' 'labels' {:table-fn {:class \"my-table-class\"}})
         ;;=> \"...<table class=\"my-table-class\"> ... </table>

         => (html-table<- 'ds' 'labels' {:thead-fn {:class \"my-thead-class\"}})
         ;;=> \"...<thead class=\"my-thead-class\"> ... </table>

       * 'th-fn' can be a map or (fn ['column-key'] ..). It's the column-key
         from the dataset not the column label, e.g.:
         => (html-table<- 'ds' 'labels' {:th-fn #(when (= % :date) {:class \"my-date\"}
         ;;=> \"...<th class=\"my-date\"> ... </th>

       * 'tr-fn' can be a map or (fn ['row-map'] ..). which are a map with the column
         keys mapped to the row values
         => (html-table<- 'ds' 'labels' {:tr-fn #(if (< 0 (:first %)) {:class \"positive\"} {:class \"negative\"}
         ;;=> \"...<tr class=\"positive\"><td> 1</td> ... </tr>
                ...<tr class=\"negative\"><td>-1</td> ... </tr>

       * 'td-fn' can be a map or (f [{:col col-key :row row-key] cell-value] ..] in
         which the first argument is a map with the table coordinate (key coordinate)
         the second is the cell value
         => (html-table<- 'ds' 'labels' {:td-fn #(when (and (= (:col %1) :status) (= %2 \"ERROR\")) {:class \"error\"}
         ;;=> \"...<th> Status </th> ...
                ...<td class=\"error\"> ERROR </td>"
  ([ds]
     (html-table<- ds identity nil))
  ([ds labels]
     (html-table<- ds labels nil))
  ([ds labels {:keys [table-fn thead-fn tbody-fn th-fn tr-fn td-fn]}]
     {:pre [(every? (some-fn nil? map? fn?)
                    [table-fn thead-fn tbody-fn th-fn tr-fn td-fn])]}
     (let [column-names (ic/col-names ds)]
       (hc/html
        [:table
         (when (map? table-fn) table-fn)
         [:thead
          (when (map? thead-fn) thead-fn)
          (for [name column-names]
            [:th
             (cond (map? th-fn) th-fn
                   (fn? th-fn) (th-fn name))
             (labels name)])]
         [:tbody
          (when (map? tbody-fn) tbody-fn)
          (for [row (ic/to-list ds)]
            [:tr
             (cond (map? tr-fn) tr-fn
                   (fn? tr-fn) (tr-fn (zipmap column-names row)))
             (let [[name1 & names] column-names
                   [col1 & cols] row]
               (list
                [:th
                 (cond (map? td-fn) td-fn
                       (fn? td-fn) (td-fn {:col name1 :row col1} col1))
                 col1]
                (for [[name col] (map list names cols)]
                  [:td
                   (cond (map? td-fn) td-fn
                         (fn? td-fn) (if-let [res (td-fn {:col name :row col1} col)] res col)
                         :else col)])))])]]))))


#_(to-table1d (sorted-map :name "Name" :age "Age" :height "Height")
            (list {:age 21 :name "John" :height 180}
                  {:age 22 :name "Wilfred" :height 182}
                  {:age 23 :name "Jack" :height 179}
                  {:age 24 :name "Daniel" :height 165}))

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
