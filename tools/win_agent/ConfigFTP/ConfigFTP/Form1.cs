using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using ConfigFtpServer;
using System.IO;
using System.Configuration;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Net;

namespace ConsoleApplication3
{
    public partial class ConfigFtp : Form
    {
        public ConfigFtp()
        {
            InitializeComponent();
        }
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new ConfigFtp());
        }
        private void OK_Click(object sender, EventArgs e)
        {
            String username = this.username.Text; String password = this.password.Text;
            int flag = 1;
            String serverHost = null;
            String serverPort = null;
            while (flag == 1)
            {
                StreamReader SR;
                String S;

                SR = File.OpenText("config.txt");
                S = SR.ReadLine();
                while (S != null)
                {
                    String[] temsString = S.Split('=');
                    switch (temsString[0])
                    {
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
                Uri uri = new Uri("ftp://"+serverHost+":"+serverPort+"/");
                try
                {   

                    FtpWebRequest ftprequest = FtpWebRequest.Create(uri) as FtpWebRequest;

                    ftprequest.Method = WebRequestMethods.Ftp.ListDirectoryDetails;
                    ftprequest.UsePassive = true;
                    ftprequest.Credentials = new NetworkCredential(username, password);
                    ftprequest.Timeout = 3000;
                    FtpWebResponse listResponse = (FtpWebResponse)ftprequest.GetResponse();

                    //MessageBox.Show(listResponse.WelcomeMessage);
                    listResponse.Close();
                    flag = 0;
                }
                catch
                {
                    MessageBox.Show("用户名或者密码不正确，请重新输入");
                    break;
                }
            }
            if (flag == 0)
            {
                Program.Run(this.username.Text,this.password.Text);
                Application.Exit();
            }
        }
        private void Cancel_Click_1(object sender, EventArgs e)
        {
            Application.Exit();
        }

        private void password_PreviewKeyDown(object sender, PreviewKeyDownEventArgs e)
        {
            if (e.KeyValue == 13)
                OK_Click(null, null);
        }

        private void username_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyValue == 13)
            {
                this.password.Focus();
            }
        }

    }
}
