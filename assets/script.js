var result = document.getElementById("result");
var loader = document.getElementById("loader");
var shortenText = document.getElementById("shorten-text");
var input = document.getElementById("input");
var shortenButton = document.getElementById("shorten-button");
var errorMessage = document.getElementById("error-message");
var qrImage = document.getElementById("qr-image");
var linkText = document.getElementById("link-text");
var copyIcon = document.getElementById("copy-icon");
var clickToCopy = document.getElementById("click-to-copy");

var state = 0
var copyClicks = 0
var lastUrlSent = ""

result.style.marginBottom = -result.clientHeight + "px";

function loadingState() {
    if (state == 1) return
    state = 1

    result.style.marginBottom = -result.clientHeight + "px";
    shortenText.style.display = "none"
    loader.style.display = "flex"
    input.readOnly = true
    shortenButton.disabled = true
    errorMessage.innerHTML = ""

    setTimeout(generateUrl, 500)
}

function displayResult(code) {
    if (state == 2) return
    state = 2

    qrImage.style.backgroundImage = "url(\"./qr/"+code+".png\")"
    linkText.innerHTML = "https://n6.lt/"+code

    result.style.marginBottom = "0px"
    shortenText.style.display = "flex"
    loader.style.display = "none"
    input.readOnly = false
    shortenButton.disabled = false
    errorMessage.innerHTML = ""
}

function defaultState(error) {
    errorMessage.innerHTML = error

    if (state == 0) return
    state = 0

    result.style.marginBottom = -result.clientHeight + "px";
    shortenText.style.display = "flex"
    loader.style.display = "none"
    input.readOnly = false
    shortenButton.disabled = false
}

function onShortenButtonClick() {
    if (state == 1 || lastUrlSent == input.value) {
        return
    }
    lastUrlSent = input.value
    loadingState()
}

async function generateUrl() {
    var url = input.value
    if (!isValidURL(url)) {
        defaultState("Invalid URL! Please enter a valid web address.")
        return
    }

    if (url.length > 512) {
        defaultState("The URL is too long! Maximum allowed length is 512 characters.")
        return
    }

    try{
        const response = await fetch("./api/genurl", {
            method: "POST",
            body: url
        })
        
        var headers = response.headers
        if (response.status == 200) {
            displayResult(await response.text())
            return
        }

        if (response.status >= 400 && response.status < 500) {
            if (response.status == 429) {
                lastUrlSent = ""
            }
            defaultState(await response.text())
            return
        }

        lastUrlSent = ""
        defaultState("Unexpected server response. Please try again later...")
            
    } catch (e) {
        lastUrlSent = ""
        defaultState("Encountered an unexpected error. Please try again later...")

    }
      
}

function isValidURL(str) {
    var a  = document.createElement('a');
    a.href = str;
    return (a.host && a.host != window.location.host);
 }
 
 function copyLink() {
     navigator.clipboard.writeText(linkText.innerHTML)
     copyClicks += 1
     const copyClicksSave = copyClicks
     copyIcon.style.marginTop = "-32px";
     clickToCopy.style.marginTop = "-24px";
     setTimeout(function () {
         if (copyClicksSave == copyClicks) {
             copyIcon.style.marginTop = "0px";
             clickToCopy.style.marginTop = "0px";
         }
     }, 1500)
 }
