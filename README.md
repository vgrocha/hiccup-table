# hiccup-table

A library to declaratively generate html tables from Clojure data structures. For now it only generates tables for 1 dimensional data structures, i.e. tables with a header row followed by one or more rows.
When declaring the table structure, you can pass functions that generate tags or calculate attributes depending on the cell content and/or key.

## Instalation
Just add

    [hiccup-table "0.2.0"]

to your project dependencies

## Usage

In order to generate a 1d table use 

```(hiccup.table/to-table1d data x-labels)```

It generates a hiccup tag structure of a table with the rows listed in 'data' and the headers defined by 'x-labels'.
'x-labels' must be an ordered collection intercalating the column key and the header content.

```
    (array-map :data-key "data label" ...)
```

or a vector

```
    [ :data-key "data label" ...] 
```

The "data label" will be the content of the header cells. The **:data-key** is the key that identifies the 'data' column to be used and also the column when applying the attribute-functions (more on this below). For example

```
=> (hiccup.table/to-table1d (list [21 "John" 180]
                                  [22 "Wilfred" 182]
                                  [23 "Jack" 179]
                                  [24 "Daniel" 165])
                            [1 "Name" 0 "Age" 2 "Height"])

```
Or in a map format

```
=> (hiccup.table/to-table1d (list {:age 21 :name "John" :height 180}
                                  {:age 22 :name "Wilfred" :height 182}
                                  {:age 23 :name "Jack" :height 179}
                                  {:age 24 :name "Daniel" :height 165})
                            [:name "Name" :age "Age" :height "Height"]) 
```

results in

```
;=> [:table nil
     [:thead nil
      [:tr nil ([:th nil "Name"] [:th nil "Age"] [:th nil "Height"])]]
     [:tbody nil
      '([:tr nil ([:td nil "John"] [:td nil 21] [:td nil 180])]
        [:tr nil ([:td nil "Wilfred"] [:td nil 22] [:td nil 182])]
        [:tr nil ([:td nil "Jack"] [:td nil 23] [:td nil 179])]
        [:tr nil ([:td nil "Daniel"] [:td nil 24] [:td nil 165])])]]
```

When rendered:

<table><thead><th>Name</th><th>Age</th><th>Height</th></thead><tbody><tr><td>John</td><td>21</td><td>180</td></tr><tr><td>Wilfred</td><td>22</td><td>182</td></tr><tr><td>Jack</td><td>23</td><td>179</td></tr><tr><td>Daniel</td><td>24</td><td>165</td></tr></tbody></table>

Optionally, as last argument, you may add a map of functions to set the table attributes. The following keys are valid:
- **:table-attrs**    map with attributes for \<table\>
- **:thead-attrs**    map with attributes for \<thead\>
- **:tbody-attrs**    map with attributes for \<tbody\>
- **:th-attrs**       map with attributes for \<th\>
                , or a (fn [label-key value]
                          where 'label-key' is the column key
                          and 'value' is the content for the \<th\> tag)
- **:data-tr-attrs** : map with attributes for the \<tr\> data rows (it is not applied to the header row, for that one, use **:thead-attrs**)
- **:data-td-attrs** : map with attributes for \<td\>
                , or a (fn [label-key value]
                          where 'label-key' is the column key
                          and 'value' is the content for the \<td\> tag)
- **:data-value-transform**: a (fn [value]) that will be applied to transform the content
                        of \<td\>.
                        If this key is nil, the original value is preserved

For example:

```
(let [attr-fns {:table-attrs {:class "mytable"}
                :thead-attrs {:id "mythead"}
                :tbody-attrs {:id "mytbody"}
                :data-tr-attrs {:class "trattrs"}
                :th-attrs (fn [label-key _] {:class (name label-key)})
                :data-td-attrs (fn [label-key val]
                                 (case label-key
                                   :height (if (<= 180 val)
                                             {:class "above-avg"}
                                             {:class "below-avg"}) nil))
                :data-value-transform (fn [label-key val]
                                        (if (= :name label-key)
                                          [:a {:href (str "/" val)} val]
                                          val))}]
  (hiccup.table/to-table1d
   '({:age 21 :name "John" :height 179}
     {:age 22 :name "Wilfred" :height 182})
   [:name "Name" :age "Age" :height "Height"]
   attr-fns))

;=> [:table {:class "mytable"}
     [:thead {:id "mythead"}
      [:tr nil ([:th {:class "name"} "Name"] [:th {:class "age"} "Age"] [:th {:class "height"} "Height"])]]
     [:tbody {:id "mytbody"}
      ([:tr {:class "trattrs"} ([:td nil [:a {:href "/John"} "John"]] [:td nil 21] [:td {:class "below-avg"} 179])]
       [:tr {:class "trattrs"} ([:td nil [:a {:href "/Wilfred"} "Wilfred"]] [:td nil 22] [:td {:class "above-avg"} 182])])]]
```
When rendered:

<table class="mytable"><thead id="mythead"><th class="name">Name</th><th class="age">Age</th><th class="height">Height</th></thead><tbody id="mytbody"><tr class="trattrs"><td><a href="/John">John</a></td><td>21</td><td class="above-avg">180</td></tr><tr class="trattrs"><td><a href="/Wilfred">Wilfred</a></td><td>22</td><td class="above-avg">182</td></tr></tbody></table>


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
