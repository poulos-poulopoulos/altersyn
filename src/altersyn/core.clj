(ns altersyn.core)





(def trama->clojure
  (comp
    (partial reduce
      (fn [x y]
        (if (seq? y)
          (concat (butlast x) (list (cons (last x) y)))
          (concat x (list y))))
      ())
    (partial map
      (fn [x]
        (cond
          (seq? x) (trama->clojure x)
          (vector? x) (vec (trama->clojure x))
          :else x)))))

(defmacro trama [& x]
  ((comp
     #(if (or (empty? %) (next %)) (cons 'do %) (first %))
     trama->clojure)
   x))





(defn translate-access [x]
  (cond
    (seq? x) (case (first x)
               >> (cons '. (map translate-access (rest x)))
               >>> (cons '.. (map translate-access (rest x)))
               (map translate-access x))
    (vector? x) (mapv translate-access x)
    :else x))

(def depar->clojure
  (comp
    first
    (partial reduce
      (fn [[x sep dep] y]
        (if (#{'. '$} y)
          [x y dep]
          (let [y (if (seq? y) y (list y))]
            (if (= '. sep)
              [(concat x y) nil 0]
              [(loop [acc (), x x, dep dep]
                 (let [acc (cons (butlast x) acc), x (last x)]
                   (if (zero? dep)
                     (reduce #(concat %2 (list %1)) (cons x y) acc)
                     (recur acc x (dec dep)))))
               nil
               (if (= '$ sep) (inc dep) dep)]))))
      [() '. 0])
    (partial map
      (fn [x]
        (cond
          (seq? x) (depar->clojure x)
          (vector? x) (vec (depar->clojure x))
          :else x)))))

(defmacro depar [& x]
  ((comp
     translate-access
     #(if (or (empty? %) (next %)) (cons 'do %) (first %))
     depar->clojure)
   x))





(def scan-with
  (comp
    (partial comp
      (partial remove empty?)
      (partial mapcat rest))
    (partial partial re-seq)
    re-pattern
    #(str "((?s).*?)(" % "|\\z)")
    (partial clojure.string/join \|)
    (partial map #(java.util.regex.Pattern/quote %))
    list))

(def split-symbols
  (partial mapcat
    (fn [x]
      (if (symbol? x)
        (map read-string ((scan-with "." "$") (name x)))
        (list (cond
                (seq? x) (split-symbols x)
                (vector? x) (vec (split-symbols x))
                :else x))))))

(defmacro depar+ [& x]
  ((comp
     translate-access
     #(if (or (empty? %) (next %)) (cons 'do %) (first %))
     depar->clojure
     split-symbols)
   x))

