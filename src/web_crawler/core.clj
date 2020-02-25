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

(defn format-url-for-var-country
  [var country]
  (str "https://crudata.uea.ac.uk/cru/data/hrg/cru_ts_4.01/crucy.1709191757.v4.01/countries/"
       var "/crucy.v4.01.1901.2016."
       country "."
       var ".per"))

(defn parse-floats
  [strings]
  (mapv #(Float/parseFloat %) strings))

(defn get-annual-data
  [data-per-row start-year finish-year]
  (let [range-to-keys (map #(keyword (str %)) (range start-year (+ 1 finish-year)))
        mapped-data (into {} (map #(into {} {(keyword (str (int (first %)))) (rest %)}) data-per-row))]
    (select-keys mapped-data range-to-keys)))

(defn get-data-for-years
  "Return data for specified variable, specified country within a specified year range (between 1901 and 2016)"
  [var country start-year finish-year]
  (when (and (and (> start-year 1900) (< start-year 2017) (< start-year finish-year))
             (and (< finish-year 2017) (> finish-year 1900) (> finish-year start-year)))
    (let [data-per-row (->> (format-url-for-var-country var country)
                            (to-csv)
                            (mapv parse-strings)
                            (drop 4)
                            (mapv parse-floats))]
      (get-annual-data data-per-row start-year finish-year))))

(get-data-for-years "wet" "Kosovo" 1990 2000)

;; “Show me the average value for [variable] for [country] between [start-year] and [finish-year]”
;; url: https://crudata.uea.ac.uk/cru/data/hrg/cru_ts_4.01/crucy.1709191757.v4.01/countries/wet/crucy.v4.01.1901.2016.Kosovo.wet.per
;; years: 1901 - 2016
