package me.earth.earthhack.impl.modules.client.translator.modes;

public enum sentMessageMode {
    English("en"),
    French("fr"),
    Korean("ko"),
    Traditional_Chinese("zh-TW"),
    Simplified_Chinese("zh-CN"),
    Russian("ru"),
    Polish("pl"),
    German("de"),
    Arabic("ar"),
    Spanish("es"),
    Portuguese("pt"),
    Italian("it"),
    Japanese("ja"),
    Dutch("nl"),
    Swedish("sv"),
    Finnish("fi"),
    Norwegian("no"),
    Danish("da"),
    Czech("cs"),
    Turkish("tr"),
    Greek("el"),
    Hebrew("he"),
    Hindi("hi"),
    Thai("th"),
    Vietnamese("vi"),
    Indonesian("id"),
    Malay("ms"),
    Tagalog("tl"),
    Ukrainian("uk"),
    Romanian("ro"),
    Hungarian("hu"),
    Bulgarian("bg"),
    Croatian("hr"),
    Slovak("sk"),
    Slovenian("sl"),
    Serbian("sr"),
    Macedonian("mk"),
    Lithuanian("lt"),
    Latvian("lv"),
    Estonian("et"),
    Disabled("disabled");

    public final String lang;

    sentMessageMode(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

}
