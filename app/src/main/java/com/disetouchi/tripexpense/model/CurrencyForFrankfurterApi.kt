package com.disetouchi.tripexpense.model

sealed class CurrencyForFrankfurterApi(
    val currencyCode: String,
    val currencySymbol: String,
    val nationalFlagEmoji: String
) {
    data object Aud : CurrencyForFrankfurterApi("AUD", "A$", "🇦🇺")
    data object Bgn : CurrencyForFrankfurterApi("BGN", "лв", "🇧🇬")
    data object Brl : CurrencyForFrankfurterApi("BRL", "R$", "🇧🇷")
    data object Cad : CurrencyForFrankfurterApi("CAD", "C$", "🇨🇦")
    data object Chf : CurrencyForFrankfurterApi("CHF", "Fr.", "🇨🇭")
    data object Cny : CurrencyForFrankfurterApi("CNY", "¥", "🇨🇳")
    data object Czk : CurrencyForFrankfurterApi("CZK", "Kč", "🇨🇿")
    data object Dkk : CurrencyForFrankfurterApi("DKK", "kr", "🇩🇰")
    data object Eur : CurrencyForFrankfurterApi("EUR", "€", "🇪🇺")
    data object Gbp : CurrencyForFrankfurterApi("GBP", "£", "🇬🇧")
    data object Hkd : CurrencyForFrankfurterApi("HKD", "HK$", "🇭🇰")
    data object Huf : CurrencyForFrankfurterApi("HUF", "Ft", "🇭🇺")
    data object Idr : CurrencyForFrankfurterApi("IDR", "Rp", "🇮🇩")
    data object Ils : CurrencyForFrankfurterApi("ILS", "₪", "🇮🇱")
    data object Inr : CurrencyForFrankfurterApi("INR", "₹", "🇮🇳")
    data object Isk : CurrencyForFrankfurterApi("ISK", "kr", "🇮🇸")
    data object Jpy : CurrencyForFrankfurterApi("JPY", "¥", "🇯🇵")
    data object Krw : CurrencyForFrankfurterApi("KRW", "₩", "🇰🇷")
    data object Mxn : CurrencyForFrankfurterApi("MXN", "Mex$", "🇲🇽")
    data object Myr : CurrencyForFrankfurterApi("MYR", "RM", "🇲🇾")
    data object Nok : CurrencyForFrankfurterApi("NOK", "kr", "🇳🇴")
    data object Nzd : CurrencyForFrankfurterApi("NZD", "NZ$", "🇳🇿")
    data object Php : CurrencyForFrankfurterApi("PHP", "₱", "🇵🇭")
    data object Pln : CurrencyForFrankfurterApi("PLN", "zł", "🇵🇱")
    data object Ron : CurrencyForFrankfurterApi("RON", "lei", "🇷🇴")
    data object Sek : CurrencyForFrankfurterApi("SEK", "kr", "🇸🇪")
    data object Sgd : CurrencyForFrankfurterApi("SGD", "S$", "🇸🇬")
    data object Thb : CurrencyForFrankfurterApi("THB", "฿", "🇹🇭")
    data object Try : CurrencyForFrankfurterApi("TRY", "₺", "🇹🇷")
    data object Usd : CurrencyForFrankfurterApi("USD", "$", "🇺🇸")
    data object Zar : CurrencyForFrankfurterApi("ZAR", "R", "🇿🇦")

    companion object {
        val all = listOf(
            Aud,
            Bgn,
            Brl,
            Cad,
            Chf,
            Cny,
            Czk,
            Dkk,
            Eur,
            Gbp,
            Hkd,
            Huf,
            Idr,
            Ils,
            Inr,
            Isk,
            Jpy,
            Krw,
            Mxn,
            Myr,
            Nok,
            Nzd,
            Php,
            Pln,
            Ron,
            Sek,
            Sgd,
            Thb,
            Try,
            Usd,
            Zar
        )

        fun fromCode(code: String): CurrencyForFrankfurterApi? {
            return all.find { it.currencyCode == code.uppercase() }
        }
    }
}