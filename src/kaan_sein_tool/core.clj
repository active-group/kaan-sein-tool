(ns kaan-sein-tool.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.string :as string]
            [clj-pdf.core :as pdf]))

(def month-map
  {"01" "Januar" "02" "Februar" "03" "März" "04" "April" "05" "Mai" "06" "Juni" "07" "Juli"
   "08" "August" "09" "September" "10" "Oktober" "11" "November" "12" "Dezember"})

;; Takes a string in csv format and return a vector containing maps {:date :duration :description}
(defn parse-csv [csv]
  (let [parsed-csv (csv/parse-csv csv)
        headers (vec (map string/lower-case (first parsed-csv)))
        headers (let [index (.indexOf headers "amount")]
                  (if (> index -1)
                    (assoc headers index "duration")
                    headers))

        rows (rest parsed-csv)
        columns (map keyword headers)
        row-data (mapv (fn [r] (zipmap columns (map #(if (= \[ (first %))
                                                       (read-string %)
                                                       %) r))) rows)
        ;;Take only :date, :description, :duration
        row-data (map (fn [row] (select-keys row [:date :duration :description])) row-data)]
    ;;If :duration is in format XX.XXh change to XX.XX
    (map (fn [row] (assoc row :duration (string/replace (:duration row) #"h" ""))) row-data)))

(defn get-data-from-file
  "Reads a file from given path, extracts employer, employee and year and month from filename
  Returns a map {:data :year :employee :employer}"
[path filename]
  (let [data (slurp path)
        [year-month employer employee] (string/split filename #"_")]
    {:data data
     :year-month year-month
     :employer employer
     :employee (first (string/split employee #"\."))}))

(defn year-month-to-month-year
  "Gets year and month (in format 'yyyy/mm' or 'yyyy-mm') returns a string like 'Mai 2017'"
  [year-month]
  (let [[year month] (if (string/includes? year-month "/")
                       (string/split year-month #"/")
                       (string/split year-month #"-"))]
    (str (get month-map month) " " year)))

(defn pdf-table [meta column-widths & rows]
  (into
    [:pdf-table meta column-widths]
    rows))

(defn pdf-cell
  [meta content]
  [:pdf-cell meta content])

(defn header
  "creates a (pdf-renderable) table for header informations"
  [employee project month-year]
  (pdf-table {:width-percent 100
               :padding 0
               :border false
               }
   [3 5 8]
   [(pdf-cell {:padding-bottom 20 :colspan 2} [:phrase {:style :bold :size 18} "Zeitnachweis"])
    (pdf-cell {:rowspan 4} [:image {:scale 50} (io/resource "ag-logo.jpg")])
    "Mitarbeiter:"]
   [employee]
   ["Kunde, Projekt:" project]
   ["Monat, Jahr:" month-year]))


(defn csv->pdf
  "Checks if path to file exists. Then procedes to gather infos from filename and file data
  to create a pdf.
  The csv-file-name should be in this format:
      'YYYY-MM_project and or employer_employee.csv'
  The data should be in the following format:
      \"Date\",\"Duration\",\"Description\"
      \"2017-05-24\",4.5,\"Testing\"
      \"2017-05-25\",3.5,\"Fixed bug\"
      \"2017-05-26\",4.5,\"Employer meeting\"
      \"2017-05-27\",1.5,\"Stuff\"
  'Date', 'Description', 'Duration' can be in lower-case.
  'Date'-values can be 'YYYY-MM-DD' or 'YYYY/MM/DD'."
  [path]
  (if (.exists (io/as-file path))
    (let [splitted-path (string/split path #"\/")
          filename (last splitted-path)
          path-without-filename (string/join "/" (butlast splitted-path))
          filename-pdf (str (first (string/split filename #"\.")) ".pdf")
          filename-and-path-pdf (if (> (count splitted-path) 1)
                                  (str path-without-filename "/" filename-pdf)
                                  filename-pdf)

          stuff (get-data-from-file path filename)
          month-year (year-month-to-month-year (:year-month stuff))
          employee (:employee stuff)
          employer (:employer stuff)
          rows (parse-csv (:data stuff))
          hours (format "%.2f" (apply + (map (comp read-string :duration) rows)))

          meta-header {:padding-bottom 8 :padding-left 2}
          meta {:padding-bottom 6 :padding-left 2}
          meta-bottom {:padding-bottom 6 :padding-left 2 :colspan 2}]
      (pdf/pdf
       [
        {:title         (str "Zeitnachweis " month-year ", " (:employer stuff) ", " (:employee stuff))
         :header        (str "Zeitnachweis " month-year ", " (:employer stuff) ", " (:employee stuff))
         :subject       "Zeitnachweis"
         :creator       "Active Group GmbH"
         :right-margin  50
         :author        "Active Group GmbH"
         :bottom-margin 25
         :left-margin   30
         :top-margin    30
         :size          "a4"
         :font {:size 11}
         :footer        "Seite"}
        [:spacer]
        (header employee employer month-year)
        [:spacer 3]
        (apply
         pdf-table {:header 
                    [[(pdf-cell meta-header [:paragraph {:style :bold} "Datum"])
                      (pdf-cell meta-header [:paragraph {:style :bold} "Beschreibung / Kommentar"])
                      (pdf-cell meta-header [:paragraph {:style :bold} "Dauer"])]]
                    :padding 0
                    :width-percent 100
                    }
         [10 60 7]
         (concat (map (fn [m] [(pdf-cell meta (:date m))
                               (pdf-cell meta (:description m))
                               (pdf-cell meta (format "%.2f" (read-string (:duration m))))]) rows)
                 [[[:pdf-cell {:padding 5 :colspan 3} ]]]
                 [[(pdf-cell meta-bottom "Gesamt") (pdf-cell meta (str hours))]]))
        ]
       filename-and-path-pdf))

    (println "Datei " path " nicht gefunden.")))

(defn -main
  [& args]
  (println "Zu bearbeitende Dateien:\n" (str "\n  " (string/join "\n  " args)))
  (doall (map csv->pdf args))
  (println "\nFertig."))
