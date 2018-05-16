const functions = require('firebase-functions');
exports.emojify =
    functions.database.ref('/messages/{pushId}/text')
    .onWrite(event => {
        if (!event.data.val() || event.data.previous.val()) {
            console.log("not a new write event");
            return;
        }
        const originalText = event.data.val();
        const emojifiedText = emojifyText(originalText);
        return event.data.ref.set(emojifiedText);
    });
function emojifyText(text) {
    var emojifiedText = text;
    emojifiedText = emojifiedText.replace(/\blol\b/ig, "😂");
    emojifiedText = emojifiedText.replace(/\bcat\b/ig, "😸");
    return emojifiedText;
}
