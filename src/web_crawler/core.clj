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

(defn get-countries-by-variable
  "Returns a list of 'countries by variable' variables to append to a cru-url"
  []
  (map #(get-in % [:attrs :href])
       (html/select (scrape-url cru-url) [:table :td :a])))

(get-countries-by-variable)
;; => '(cld dtr frs pet pre tmn tmp tmx vap wet)
