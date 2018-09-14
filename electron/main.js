const electron = require('electron')
const app = electron.app;
const BrowserWindow = electron.BrowserWindow;
const path = require('path');
const url = require('url');
const { ipcMain } = require('electron');
const { Menu } = require('electron');
const https = require('https');
const request = require('request');

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let win
let useDevelopmentServer;       //bool, whether angular is serving the file or whether we are accessing it statically

function createWindow() {
    // Create the browser window.
    //`file://${__dirname}/dist/assets/logo.png`
    if (useDevelopmentServer) {
        win = new BrowserWindow({ width: 800, height: 600 , icon: 'http://127.0.0.1:4200/assets/logo.png'});
        win.loadURL(url.format({
            //pathname: path.join(__dirname, '/app/index.html'),
            //'${__dirname}/dist/index.html' //for angular
            pathname: '127.0.0.1:4200/',
            protocol: 'http:',
            slashes: true
        }));
    }

    else {
        win = new BrowserWindow({ width: 800, height: 600 , icon: 'file://' + path.join(__dirname, '/dist/electron/assets/logo.png')});

        // and load the index.html of the app.
        win.loadURL(url.format({
            //pathname: path.join(__dirname, '/app/index.html'),
            //'${__dirname}/dist/index.html' //for angular
            pathname: path.join(__dirname, '/dist/electron/index.html'),
            protocol: 'file:',
            slashes: true
        }));
    }

    // Open the DevTools.
    win.webContents.openDevTools()

    // Emitted when the window is closed.
    win.on('closed', () => {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        win = null
    })
}
function parseArgs() {
    useDevelopmentServer = null;
    process.argv.forEach(function (val, index) {
        //Index 2, whether angular is serving the file already
        if (index == 2) {
            console.log("value of useDevServer " + val);
            if (val == true)
                useDevelopmentServer = true;
            else
                useDevelopmentServer = false;
        }
      });
    if (useDevelopmentServer == null) {
        useDevelopmentServer = false;
        console.error("Didn't specify useDevelopmentServer argument before running, treating as false");
    }
    console.log("useDevelopment server is " + useDevelopmentServer);
}
function shittySleep(cycles) {
    for (var i = 0; i < cycles; i++) {
        for (var j = 0; j < cycles; j++) {
            for (var k = 0; k < cycles; k++) {
                //do nothing and feel guity about it
            }
        }
    }
}
function waitForServer() {
    for (var i = 0; i < 10; i++) {
        //try {
        var status = request.get('http://127.0.0.1:4200')
            .on('response', function(response) {
                if (response.statusCode == 200)
                    return true;
                return false;
            })
            .on('error', function(err) {
                console.log("Not ready on attempt " + i);
                return false;
            });
        if (status == true) {
            break;
        }
        //catch (err) {}
        shittySleep(1000);
    }
}

parseArgs();
if (useDevelopmentServer)
    waitForServer();
// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', createWindow)

// Quit when all windows are closed.
app.on('window-all-closed', () => {
    // On macOS it is common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
        app.quit()
    }
})

app.on('activate', () => {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (win === null) {
        createWindow()
    }
})

  // In this file you can include the rest of your app's specific main process
  // code. You can also put them in separate files and require them here.