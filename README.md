# RadQRCode

## *Using QR Codes for recording and sharing teaching file metadata on mobile devices*
 
This is the mobile portion of an application set designed to facilitate recording case metadata for personal teaching files.

This Android mobile app contains a QR code scanner that scans the code from the desktop application, incorporating the data into an SQLite database.  The data can be searched and edited, and new QR codes can be generated for sharing between mobile devices.  Follow-up cases are flagged by color.  Data can be imported from and exported to CSV files on the Android filesystem.

This project uses code from the following open source projects:

* [ZXing 1D/2D barcode image processing library](http://code.google.com/p/zxing/)
* [OpenCSV](http://sourceforge.net/projects/opencsv/)
