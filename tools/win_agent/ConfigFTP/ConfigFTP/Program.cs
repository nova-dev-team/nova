using System;
using System.IO;
using System.Text;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Forms;
using System.Drawing;

namespace ConfigFtpServer
{
    class Program
    {   
        [DllImport("shell32.dll")]
        public static extern int ShellExecute(
                   IntPtr hwnd,
                   StringBuilder lpszOp,
                   StringBuilder lpszFile,
                   StringBuilder lpszParams,
                   StringBuilder lpszDir,
                   int FsShowCmd);

        [DllImport("User32.dll", EntryPoint = "FindWindow")]
        private static extern IntPtr FindWindow(string lpClassName, string lpWindowName);


        [DllImport("user32.dll", EntryPoint = "FindWindowEx")]
        private static extern IntPtr FindWindowEx(IntPtr hwndParent, IntPtr hwndChildAfter, string lpszClass,
                                                  string lpszWindow);

        [DllImport("user32.dll")]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetForegroundWindow(IntPtr hWnd);

        [DllImport("User32.dll", EntryPoint = "SendMessage")]
        private static extern int SendMessage(IntPtr hWnd, String Msg, IntPtr wParam, string lParam);

        [StructLayout(LayoutKind.Sequential)]
        struct NativeRECT
        {
            public int left;
            public int top;
            public int right;
            public int bottom;
        }

        [Flags]
        enum MouseEventFlag : uint
        {
            Move = 0x0001,
            LeftDown = 0x0002,
            LeftUp = 0x0004,
            RightDown = 0x0008,
            RightUp = 0x0010,
            MiddleDown = 0x0020,
            MiddleUp = 0x0040,
            XDown = 0x0080,
            XUp = 0x0100,
            Wheel = 0x0800,
            VirtualDesk = 0x4000,
            Absolute = 0x8000
        }

        [DllImport("user32.dll")]
        static extern bool SetCursorPos(int X, int Y);

        [DllImport("user32.dll")]
        static extern void mouse_event(MouseEventFlag flags, int dx, int dy, uint data, UIntPtr extraInfo);

        [DllImport("user32.dll")]
        private static extern void GetWindowRect(IntPtr hwnd, out NativeRECT rect);
  
        static int OpenFTPDrive() //打开程序
        {
            String filename = "FtpDrive.exe";
            String dirname = @"C:\Program Files\KillSoft\FtpDrive\";
            StreamReader SR;
            String S;
            SR = File.OpenText("config.txt");
            S = SR.ReadLine();
            String[] tempString = S.Split('=');
            if (!tempString[1].Equals("default"))
                dirname = "@"+tempString[1];
            SR.Close();

            int result = ShellExecute(IntPtr.Zero,
                 new StringBuilder("Open"),
                 new StringBuilder(filename),
                 new StringBuilder(""),
                 new StringBuilder(dirname), 5);
            //Console.WriteLine("{0}!", result);
            return result;
        }

        public static void LocateButton(String dlgTitle)
        {
            Point endPosition = new Point();
            NativeRECT rect;
            IntPtr parentWnd = FindWindow(null, dlgTitle);
            // periodically check whether download-task-dlg is opened

            while (parentWnd == (IntPtr)0)
            {
                
                parentWnd = FindWindow(null, dlgTitle);
                Thread.Sleep(500);
            }
            SetForegroundWindow(parentWnd);
            IntPtr hButton = FindWindowEx(parentWnd, IntPtr.Zero, "BUTTON", null);
           
            {
                GetWindowRect(hButton, out rect);
                endPosition.X = (rect.left + rect.right) / 2;
                endPosition.Y = (rect.top + rect.bottom) / 2-50;
                SetCursorPos(endPosition.X, endPosition.Y);
                Thread.Sleep(500);
                mouse_event(MouseEventFlag.LeftDown, 0, 0, 0, UIntPtr.Zero);
                mouse_event(MouseEventFlag.LeftUp, 0, 0, 0, UIntPtr.Zero);

            }
        }


        public static void ConfigSite(String username,String password)
        {
            Point endPosition = new Point();
            NativeRECT rect;
            String dlgTitle = "FTPDrive v3.5 settings";
            String dilText = "FTP sites list editor";
            IntPtr parentWnd = FindWindow(null, dilText);
            while (parentWnd == (IntPtr)0)
            {

                parentWnd = FindWindow(null, dilText);
                Thread.Sleep(1000);
            }
            SetForegroundWindow(parentWnd);
            IntPtr hText1 = FindWindowEx(parentWnd, IntPtr.Zero, "EDIT", null);
            //Display name
            GetWindowRect(hText1, out rect);
            endPosition.X = (rect.left + rect.right) / 2;
            endPosition.Y = (rect.top + rect.bottom) / 2;
            SetCursorPos(endPosition.X, endPosition.Y);
            mouse_event(MouseEventFlag.LeftDown, 0, 0, 0, UIntPtr.Zero);
            mouse_event(MouseEventFlag.LeftUp, 0, 0, 0, UIntPtr.Zero);
            //Thread.Sleep(500);

            String serverName = null;
            String serverHost = null;
            String serverPort = null;

            StreamReader SR;
            String S;

            SR = File.OpenText("config.txt");
            S = SR.ReadLine();
            while (S != null)
            {
                String[] temsString = S.Split('=');
                switch (temsString[0])
                {
                    case "server name":
                        serverName = temsString[1];
                        break;
                    case "server host":
                        serverHost = temsString[1];
                        break;
                    case "server port":
                        serverPort = temsString[1];
                        break;
                    default:
                        break;
                }

                //Console.WriteLine(temsString[1]);
                S = SR.ReadLine();
            }
            SR.Close();
            //server name
            Thread.Sleep(100);
            SendKeys.SendWait(serverName); SendKeys.SendWait("{TAB}"); Console.WriteLine(serverName);
            //server host
            Thread.Sleep(100);
            SendKeys.SendWait(serverHost); SendKeys.SendWait("{TAB}"); Console.WriteLine(serverHost);
            //server port
            Thread.Sleep(100);
            SendKeys.SendWait(serverPort); SendKeys.SendWait("{TAB}"); Console.WriteLine(serverPort);
            //user name
            Thread.Sleep(100);
            SendKeys.SendWait(username); SendKeys.SendWait("{TAB}");
            //password
            Thread.Sleep(100);
            SendKeys.SendWait(password); SendKeys.SendWait("{TAB}");
            // home directory
            SendKeys.SendWait("{TAB}");
            //connection limit

            IntPtr addButton = FindWindowEx(parentWnd, IntPtr.Zero, "BUTTON", "Add site");
            GetWindowRect(addButton, out rect);
            endPosition.X = (rect.left + rect.right) / 2;
            endPosition.Y = (rect.top + rect.bottom) / 2;
            SetCursorPos(endPosition.X, endPosition.Y);
            mouse_event(MouseEventFlag.LeftDown, 0, 0, 0, UIntPtr.Zero);
            mouse_event(MouseEventFlag.LeftUp, 0, 0, 0, UIntPtr.Zero);

            IntPtr OKButton= FindWindowEx(parentWnd, IntPtr.Zero, "BUTTON", "OK");
            GetWindowRect(OKButton, out rect);
            endPosition.X = (rect.left + rect.right) / 2;
            endPosition.Y = (rect.top + rect.bottom) / 2;
            SetCursorPos(endPosition.X, endPosition.Y);
            mouse_event(MouseEventFlag.LeftDown, 0, 0, 0, UIntPtr.Zero);
            mouse_event(MouseEventFlag.LeftUp, 0, 0, 0, UIntPtr.Zero);

            IntPtr parentWnd1 = FindWindow(null, dlgTitle);
            while (parentWnd1 == (IntPtr)0)
            {

                parentWnd1 = FindWindow(null, dlgTitle);
                Thread.Sleep(1000);
            }

            IntPtr okButton= FindWindowEx(parentWnd1, IntPtr.Zero, "BUTTON", "OK");
            GetWindowRect(okButton, out rect);
            endPosition.X = (rect.left + rect.right) / 2;
            endPosition.Y = (rect.top + rect.bottom) / 2;
            SetCursorPos(endPosition.X, endPosition.Y);
            mouse_event(MouseEventFlag.LeftDown, 0, 0, 0, UIntPtr.Zero);
            mouse_event(MouseEventFlag.LeftUp, 0, 0, 0, UIntPtr.Zero);

                
        }

        

        public static void Run(String username ,String password)
        {
            String dlgTitle = "FTPDrive v3.5 settings";
            OpenFTPDrive();//打开Ftpdrive.exe
            OpenFTPDrive();//打开FTPDrive v3.5 settings
            LocateButton(dlgTitle);
            ConfigSite(username, password);
        }
    }
}
