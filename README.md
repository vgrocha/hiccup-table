# hiccup-table

A library to easily generate html tables from clojure data structures with the possibility of defining the tags atributes as functions/maps.

## Instalation
Just add

    [hiccup-table "0.1.0"]

to your project dependencies

## Usage

If you want to generate a table with the first row as header you can use ```(fn hiccup.table/to-table1d [data x-labels])```. It generates a hiccup tag structure of a table with the header row 'x-labels' and the 'data'.
The header row is defined by 'x-labels', which can be a an array-map (must be ordered, for it will be the columns order) in the format
 
    (array-map :data-key "data label" ...)

or a vector

    [ :data-key "data label" ...] 

The "data label" is the value on the header cell. The **:data-key** is the key that identifies the 'data' column to be used and also the column when applying the attribute-functions (more on this below). For example

    => (hiccup.table/to-table1d (list {:age 21 :name "John" :height 180}
                                      {:age 22 :name "Wilfred" :height 182}
                                      {:age 23 :name "Jack" :height 179}
                                      {:age 24 :name "Daniel" :height 165})
                                [:name "Name" :age "Age" :height "Height"]) 

Or in a vector format

    => (hiccup.table/to-table1d (list [21 "John" 180]
                                      [22 "Wilfred" 182]
                                      [23 "Jack" 179]
                                      [24 "Daniel" 165])
                                [1 "Name" 0 "Age" 2 "Height"])
                              
Results

    ;=> [:table nil
          [:thead nil
           ([:th nil "Name"] [:th nil "Age"] [:th nil "Height"])]
          [:tbody nil
           ([:tr nil ([:td nil "John"] [:td nil 21] [:td nil 180])]
            [:tr nil ([:td nil "Wilfred"] [:td nil 22] [:td nil 182])]
            [:tr nil ([:td nil "Jack"] [:td nil 23] [:td nil 179])]
            [:tr nil ([:td nil "Daniel"] [:td nil 24] [:td nil 165])])]]

When rendered

<table><thead><th>Name</th><th>Age</th><th>Height</th></thead><tbody><tr><td>John</td><td>21</td><td>180</td></tr><tr><td>Wilfred</td><td>22</td><td>182</td></tr><tr><td>Jack</td><td>23</td><td>179</td></tr><tr><td>Daniel</td><td>24</td><td>165</td></tr></tbody></table>

Optionally, as last argument, you may add a map of functions for setting the table attributes. The following keys are valid:

* **:table-attrs**: a map with the attributes for the \<table\> tag, e.g. {:class "mytable"}
* **:thead-attrs**: a map with the attributes for the \<thead\> tag, e.g. {:class "mythead"}
* **:tbody-attrs**: a map with the attributes for the \<tbody\> tag, e.g. {:class "mytbody"}
* **:th-fn**: a map with the attributes for the \<th\> tag on the header or a (fn [label-key]) which returns the attributes given that 'label-key' is the column key from 'x-labels'
* **:tr-attrs**: a map with the attributes for the \<tr\> tag, e.g. {:class "trattrs"}
* **:td-fn**: a map with attributes for the \<td\> tag or a function (fn [label-key value]), where the 'label-key' is the column key from 'x-labels' and the value is the \<td\> ... \</td\> cell content
* **:val-fn**: must be a function (fn [label-key val]) which returns the cell value inside \<td\> ... \</td\>. Use it for example for adding a link 

     (fn [label-key val] 
         [:a {:href val} val]) 
        
   inside the cell. If this function is not set (i.e. nil) the value from the 'data' is returned.

For example:

    => (def attrs-fns {:table-attrs {:class "mytable"}
                       :thead-attrs {:id "mythead"}
                       :tbody-attrs {:id "mytbody"}
                       ;;The class of th is the 'label' key, without the ":"
                       :th-fn (fn [label-key] {:class (subs (str label-key) 1)})
                       :tr-attrs {:class "trattrs"}
                       ;;if the column is :height, sets the class according to the
                       ;;value
                       :td-fn (fn [label-key val]
                                (case label-key
                                  :height (if (<= 180 val)
                                            {:class "above-avg"}
                                            {:class "below-avg"}) nil))
                       :val-fn (fn [label-key val]
                                 (if (= :name label-key)
                                   [:a {:href (str "/" val)} val]
                                   val))}
                                   
    => (hiccup.table/to-table1d (list {:age 21 :name "John" :height 180}
                                    {:age 22 :name "Wilfred" :height 182})
                              [:name "Name" :age "Age" :height "Height"] attrs-fns))
                              
    ; => [:table {:class "mytable"}
          [:thead {:id "mythead"}
           ([:th {:class "name"} "Name"] [:th {:class "age"} "Age"] [:th {:class "height"} "Height"])]
          [:tbody {:id "mytbody"}
           ([:tr {:class "trattrs"} ([:td nil [:a {:href "/John"} "John"]] [:td nil 21] [:td {:class "above-avg"} 180])]
            [:tr {:class "trattrs"} ([:td nil [:a {:href "/Wilfred"} "Wilfred"]] [:td nil 22] [:td {:class "above-avg"} 182])])]]

When rendered:

<table class="mytable"><thead id="mythead"><th class="name">Name</th><th class="age">Age</th><th class="height">Height</th></thead><tbody id="mytbody"><tr class="trattrs"><td><a href="/John">John</a></td><td>21</td><td class="above-avg">180</td></tr><tr class="trattrs"><td><a href="/Wilfred">Wilfred</a></td><td>22</td><td class="above-avg">182</td></tr></tbody></table>


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
