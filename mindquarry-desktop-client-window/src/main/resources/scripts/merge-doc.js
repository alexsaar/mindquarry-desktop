var objArgs, num, sTheirDoc, sMergedDoc, word, baseDoc;

// Microsoft Office versions for Microsoft Windows OS
var vOffice2000 = 9;
var vOffice2002 = 10;
var vOffice2003 = 11;
var vOffice2007 = 12;

// WdCompareTarget
var wdCompareTargetSelected = 0; // save in server version (file.doc.rXXX) -> document in Merge() call
var wdCompareTargetCurrent = 1; // save in normal version (file.doc) -> baseDoc
var wdCompareTargetNew = 2; // save as new file (needs 'save as' dialog)

objArgs = WScript.Arguments;
num = objArgs.length;
if (num < 2) {
   WScript.Echo("Usage: [CScript|WScript] merge-doc.js merged.doc their.doc");
   WScript.Quit(1);
}
sMergedDoc = objArgs(0); // eg. file.doc
sTheirDoc = objArgs(1);  // eg. file.doc.rXXX

try {
	word = WScript.CreateObject("Word.Application");
} catch(e) {
	Wscript.Echo("You must have Microsoft Word installed to perform this operation.");
	Wscript.Quit(2);
}

word.visible = true

// Open the base document
baseDoc = word.Documents.Open(sMergedDoc);

// Now merge the other document into the base doc
if (Number(word.Version) < vOffice2003) {
	// older word versions do not support the Document.Merge() method, so we
	// fall back to a Compare that exists since 
	baseDoc.Compare(sTheirDoc);
} else {
	// This is an asynchronous call as the user will be presented the word
	// interface and he will have to select the changes to apply, maybe edit
	// the document and press save (or close and save). The document will be
	// saved as sMergedDoc (because of using wdCompareTargetCurrent).
	baseDoc.Merge(sTheirDoc, wdCompareTargetCurrent);
}
