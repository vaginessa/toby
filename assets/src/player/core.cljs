(ns player.core)

(def player (atom nil))
(def video-title (atom nil))
(def socket (js/io "http://localhost:5150"))

(def tag (.createElement js/document "script"))
(def first-script-tag (first (array-seq (.getElementsByTagName js/document "script"))))

(defn on-player-state-change [e]
 (let [video-info (.getVideoData (.-target e))]
   (when-not (= video-title video-info.title)
     (do
      (reset! video-title video-info.title)
      (js/console.log video-info)
      (.emit socket "video-info" video-info)))))

;(do
; (.emit socket "youtube-player-state-changed" #js {
;    :title video-info.title
;   :video-id video-info.id })

(defn on-youtube-api-ready []
 (do
   (.emit socket "youtube-api-ready")
   (.on socket "play" (fn [ytid]
     (if (nil? @player)
       (reset! player (YT.Player. "player" #js {
         :videoId ytid
         :playerVars #js { :autoplay 1 :autohide 1 }
         :events #js { :onStateChange on-player-state-change }}))
       (do
         (js/console.log (str "loading ytid: " ytid))
         (.loadVideoById @player ytid)))))))

(aset js/window "onYouTubeIframeAPIReady" on-youtube-api-ready)
(set! (.-src tag) "https://www.youtube.com/iframe_api")
(.insertBefore (.-parentNode first-script-tag) tag first-script-tag)
