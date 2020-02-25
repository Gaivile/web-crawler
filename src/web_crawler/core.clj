(ns web-crawler.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.data.csv :as csv]))

;; Url for Climate Research Unit
(def cru-url
  "https://crudata.uea.ac.uk/cru/data/hrg/cru_ts_4.01/crucy.1709191757.v4.01/countries/")

(defn scrape-url
  "Entry point to ingest the data from given url"
  [url]
  (html/html-resource (java.net.URL. url)))

(defn extract-url-list
  "Returns a list of urls from a given site. Use cases:
    (1) 'countries by variable' variables to append to a cru-url
    (2) get filenames by a variable (list of countries per variable)"
  [url]
  (map #(get-in % [:attrs :href])
       (html/select (scrape-url url) [:table :td :a])))

(defn var-to-files
  "Map variables to their list of extracted filenames"
  [var]
  {(keyword var)
   (extract-url-list (str cru-url var "/"))})

(defn get-all-files
  []
  (map var-to-files (extract-url-list cru-url)))

(defn to-csv
  "Returns a sequence of vectors, each vector contains a string of a specific line"
  [url]
  (csv/read-csv
   (slurp url)))

(defn parse-strings
  "Return a list of strings"
  [data-csv]
  (re-seq #"[a-zA-Z0-9\.]+" (first data-csv)))

(mapv #(mapv #(Float/parseFloat  %) %) (drop 4 (map parse-strings (to-csv (format-url-for-var-country "cld" "Mexico")))))


(defn format-url-for-var-country
  [var country]
  (str "https://crudata.uea.ac.uk/cru/data/hrg/cru_ts_4.01/crucy.1709191757.v4.01/countries/"
       var "/crucy.v4.01.1901.2016."
       country "."
       var ".per"))


(println (to-csv (format-url-for-var-country "cld" "Mexico")))

#_(defn get-data-for-years
  [var country start-year finish-year]
  (let [url (format-url-for-var-country var country)]))



;; “Show me the average value for [variable] for [country] between [start-year] and [finish-year]”
;; https://crudata.uea.ac.uk/cru/data/hrg/cru_ts_4.01/crucy.1709191757.v4.01/countries/wet/crucy.v4.01.1901.2016.Kosovo.wet.per


(parse-strings (nth (to-csv (format-url-for-var-country "cld" "Mexico")) 5))

(def data-file (to-csv (format-url-for-var-country "cld" "Mexico")))
