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
