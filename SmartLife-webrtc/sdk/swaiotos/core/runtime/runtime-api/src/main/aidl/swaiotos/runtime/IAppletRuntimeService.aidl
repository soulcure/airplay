// IAppletRuntimeService.aidl
package swaiotos.runtime;

// Declare any non-default types here with import statements

interface IAppletRuntimeService {
    int startApplet(in Uri applet);
    int startApplet2(in Uri applet, in Bundle bundle);
}
