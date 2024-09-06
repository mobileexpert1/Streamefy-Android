package com.streamefy.component.ui.home.model


import com.google.gson.annotations.SerializedName

data class ProjectItem(@SerializedName("projectDescription")
                       val projectDescription: String = "",
                       @SerializedName("projectId")
                       val projectId: Int = 0,
                       @SerializedName("projectTitle")
                       val projectTitle: String = "")


data class BackgroundMediaItem(@SerializedName("alterDate")
                               val alterDate: String = "",
                               @SerializedName("thumbnailFileName")
                               val thumbnailFileName: String = "",
                               @SerializedName("format")
                               val format: String = "",
                               @SerializedName("bunnyId")
                               val bunnyId: String = "",
                               @SerializedName("videoS3bucketId")
                               val videoSBucketId: String = "",
                               @SerializedName("thumbnailS3bucketId")
                               val thumbnailSBucketId: String = "",
                               @SerializedName("mediaProjectId")
                               val mediaProjectId: Int = 0,
                               @SerializedName("size")
                               val size: String = "",
                               @SerializedName("hlsPlaylistUrl")
                               val hlsPlaylistUrl: String = "",
                               @SerializedName("thumbnailId")
                               val thumbnailId: String = "",
                               @SerializedName("id")
                               val id: Int = 0,
                               @SerializedName("videoFileName")
                               val videoFileName: String = "",
                               @SerializedName("createDate")
                               val createDate: String = "")


data class MediaItem(@SerializedName("size")
                     val size: String = "",
                     @SerializedName("format")
                     val format: String = "",
                     @SerializedName("hlsPlaylistUrl")
                     val hlsPlaylistUrl: String = "",
                     @SerializedName("description")
                     val description: String = "",
                     @SerializedName("bunnyId")
                     val bunnyId: String = "",
                     @SerializedName("thumbnailS3bucketId")
                     val thumbnailS3bucketId: String = "",
                     @SerializedName("id")
                     val id: Int = 0)


data class HomeResponse(@SerializedName("data")
                        val data: Data,
                        @SerializedName("itemsPerPage")
                        val itemsPerPage: Int = 0,
                        @SerializedName("totalCount")
                        val totalCount: Int = 0,
                        @SerializedName("currentPage")
                        val currentPage: Int = 0,
                        @SerializedName("error")
                        val error: Error,
                        @SerializedName("isSuccess")
                        val isSuccess: Boolean = false)


data class EventsItem(@SerializedName("eventId")
                      val eventId: Int = 0,
                      @SerializedName("eventTitle")
                      val eventTitle: String = "",
                      @SerializedName("medias")
                      val media: List<MediaItem>?,
                      @SerializedName("userName")
                      val userName: String = "",
                      @SerializedName("userId")
                      val userId: Int = 0)


data class Error(@SerializedName("userMessage")
                 val userMessage: String = "",
                 @SerializedName("developerMessage")
                 val developerMessage: String = "",
                 @SerializedName("innerException")
                 val innerException: String = "",
                 @SerializedName("errorCode")
                 val errorCode: String = "",
                 @SerializedName("stackTrace")
                 val stackTrace: String = "",
                 @SerializedName("error")
                 val error: String = "")


data class Data(@SerializedName("totalMedia")
                val totalMedia: Int = 0,
                @SerializedName("backgroundMedia")
                val backgroundMedia: List<BackgroundMediaItem>?,
                @SerializedName("totalEvents")
                val totalEvents: Int = 0,
                @SerializedName("itemsPerPage")
                val itemsPerPage: Int = 0,
                @SerializedName("project")
                val project: List<ProjectItem>?,
                @SerializedName("currentPage")
                val currentPage: Int = 0,
                @SerializedName("events")
                val events: List<EventsItem>?)


