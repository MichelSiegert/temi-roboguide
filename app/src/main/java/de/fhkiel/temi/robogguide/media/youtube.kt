package de.fhkiel.temi.robogguide.media

fun createYoutube(url: String): String{
    return "<iframe width=\"600\" height=\"400\"" +
            " src=\"${url}\" " +
            "title=\"VID\" " +
            "frameborder=\"0\" " +
            "allow=\"accelerometer;" +
            " autoplay; " +
            "clipboard-write; " +
            "encrypted-media; " +
            "gyroscope; " +
            "picture-in-picture; " +
            "web-share\" " +
            "referrerpolicy=\"strict-origin-when-cross-origin\" " +
            "allowfullscreen>" +
            "</iframe>";
}