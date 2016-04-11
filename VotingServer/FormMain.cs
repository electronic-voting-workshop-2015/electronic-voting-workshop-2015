using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Runtime.Serialization.Json;
using System.Web.Script.Serialization;
using System.Threading;
using System.Net;
using System.Diagnostics;
using Newtonsoft.Json;

namespace VotingRegisterationClient
{
    public partial class FormMain : Form
    {
        string ByteArrayToUtf8(byte[] arr)
        {
            return Encoding.UTF8.GetString(arr);
        }
        bool bShouldExit = false;
        void ListenIncoming()
        {
            HttpListener listener;
            Thread t1 = null;
            listener = new HttpListener();
            listener.Prefixes.Add("http://*:4567/");
            listener.Start();
            while (true)
            {
                Task<HttpListenerContext> context = listener.GetContextAsync();
                bool bNeedToContinueWait = true;
                while (bNeedToContinueWait)
                {
                    bNeedToContinueWait = !context.Wait(100);
                    if (bShouldExit)
                    {
                        break;
                    }
                }
                if (bShouldExit)
                {
                    break;
                }
                HttpListenerRequest request = context.Result.Request;
                // Obtain a response object.
                HttpListenerResponse response = context.Result.Response;
                response.AddHeader("Access-Control-Allow-Origin", "*");
                response.AddHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
                response.AddHeader("Access-Control-Max-Age", "1000");
                response.AddHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, X-Requested-With");
                response.StatusCode = (int)HttpStatusCode.OK;
                StreamReader s1 = new StreamReader(request.InputStream);
                string sRes = s1.ReadToEnd();
                response.Close();
                
                if (request.HttpMethod == "POST")
                {
                    //var x = Convert.ToBase64String(Encoding.UTF8.GetBytes(sRes));
                    //var y = Encoding.UTF8.GetString(Convert.FromBase64String(x));
                    var json = sRes.Replace("\"", "\"\"\"");
                    System.Diagnostics.Process clientProcess = new Process();
                    clientProcess.StartInfo.FileName = "java";
                    if (request.Url.AbsoluteUri.Contains("/Vote"))
                    {
                        clientProcess.StartInfo.Arguments = @"-jar " + @".\sadna.jar vote " + json;
                        clientProcess.Start();
                    }
                    if (request.Url.AbsoluteUri.Contains("/Audit"))
                    {
                        clientProcess.StartInfo.Arguments = @"-jar " + @".\sadna.jar audit " + json;
                        clientProcess.Start();
                    }
                }
                //string responseString = "<HTML><BODY> Hello world!</BODY></HTML>";
                //byte[] buffer = System.Text.Encoding.UTF8.GetBytes(responseString);
                //// Get a response stream and write the response to it.
                //response.ContentLength64 = buffer.Length;
                //System.IO.Stream output = response.OutputStream;
                //output.Write(buffer, 0, buffer.Length);


                //  string responseString = "<HTML><BODY> Hello world! " + request.QueryString.AllKeys[0].ToString() + " Type " + request.HttpMethod + " url " + request.Url.AbsolutePath.ToString() + "</BODY></HTML>";
                // byte[] buffer = System.Text.Encoding.UTF8.GetBytes(responseString);
                // Get a response stream and write the response to it.
                //response.ContentLength64 = buffer.Length;
                //    System.IO.Stream output = response.OutputStream;
                // output.Write(buffer, 0, buffer.Length);
                //  output.Close();
            }

        }

        public FormMain()
        {
            //System.Diagnostics.Process clientProcess = new Process();
            //clientProcess.StartInfo.FileName = "java";
            //clientProcess.StartInfo.Arguments = "-jar \"C:\\Users\\ben\\Desktop\\VotingRegisterationClient - Copy\\VotingRegisterationClient\\sadna.jar\" vote [{\"machineId\":\"1db8136f-9978-44fd-b957-f6dee8817f54\"},{\"position\":\"ed\",\"type\":0,\"chosenCandidates\":[\"הליכוד\"]},{\"position\":\"מפלגה1\",\"type\":1,\"chosenCandidates\":[\"הליכוד\"]},{\"position\":\"ed3\",\"type\":2,\"chosenCandidates\":[\"הליכוד\"]}]";
            //clientProcess.Start();
           // InitializseComponent();
            ListenIncoming();
        }

        LogOnForm m_LoginForm;
        bool m_bIsLogIn;

        private bool Connect(string sName, string sClientAddr, string sServerAddr, string sBBAddr, string sBBSig)
        {
            allVotersDB = new VotersDB();
            AllCurrentVoters = new Voter[0];
            AllCurrentVotersPanels = new Panel[0];
            serverCom = new Communicator();
            string sError;
            bool bSuccess = serverCom.OpenConnection(sName, sClientAddr, sServerAddr, sBBAddr, sBBSig, allVotersDB, out sError);
            if (bSuccess == false)
            {
                serverCom.CloseConnection();
                serverCom = null;
                MessageBox.Show("Could not connect to server! " + sError, "Fatal Error");
                return false;
            }

            bSuccess = serverCom.SignUpClient(out sError);
            if (bSuccess == false)
            {
                serverCom.CloseConnection();
                serverCom = null;
                MessageBox.Show("Could not sign up the client! " + sError, "Fatal Error");
                return false;
            }
            m_bIsLogIn = true;
            m_LoginForm.Close();
            MessageBox.Show("Press OK only when all voters were added to the server. The client will receive its voter list on pressing OK.", "Voter List Update");

            if (false == serverCom.GetVoterListFromServer(out sError))
            {
                serverCom.CloseConnection();
                serverCom = null;
                MessageBox.Show("Failed getting voters from server. exiting. " + sError, "Fatal Error");
                return false;
            }
            return true;
        }

        private bool Reconnect(string sClientID, string sClientAddr, string sServerAddr, string sBBAddr, string sBBSig)
        {
            allVotersDB = new VotersDB();
            AllCurrentVoters = new Voter[0];
            AllCurrentVotersPanels = new Panel[0];
            serverCom = new Communicator();
            string sError;
            bool bSuccess = serverCom.OpenConnection("reconnectnoname", sClientAddr, sServerAddr, sBBAddr, sBBSig, allVotersDB, out sError);
            if (bSuccess == false)
            {
                serverCom.CloseConnection();
                serverCom = null;
                MessageBox.Show("Could not connect to server! " + sError, "Fatal Error");
                return false;
            }
            bSuccess = serverCom.Reconnect(Int32.Parse(sClientID), out sError);
            if (bSuccess == false)
            {
                serverCom.CloseConnection();
                serverCom = null;
                MessageBox.Show("Could not reconnect the client! " + sError, "Fatal Error");
                return false;
            }

            m_bIsLogIn = true;
            m_LoginForm.Close();
            MessageBox.Show("Press OK only when all voters were added to the server. The client will receive its voter list on pressing OK.", "Voter List Update");

            if (false == serverCom.GetVoterListFromServer(out sError))
            {
                serverCom.CloseConnection();
                serverCom = null;
                MessageBox.Show("Failed getting voters from server. exiting. " + sError, "Fatal Error");
                return false;
            }
            return true;
        }



        VotersDB allVotersDB;
        Voter currentSearchedVoter;
        Voter[] AllCurrentVoters;
        Panel[] AllCurrentVotersPanels;
        Communicator serverCom;
        private PersonInformation GetPersonalInfoFromVoter(Voter v1)
        {
            PersonInformation pi = new PersonInformation();
            pi.m_image = v1.ProfileImage;
            pi.m_sID = v1.lID.ToString();
            pi.m_sName = v1.sName;
            return pi;
        }

        public void CreateVoterCopy(ref Panel outVoterPanel, PersonInformation info)
        {
            outVoterPanel = new Panel();
            panelVoters.Controls.Add(outVoterPanel);
            // 
            // panelVoter
            // 
            outVoterPanel.BorderStyle = this.panelVoter.BorderStyle;
            outVoterPanel.Location = this.panelVoter.Location;
            outVoterPanel.Name = this.panelVoter.Name + "_" + info.m_sID;
            outVoterPanel.Size = this.panelVoter.Size;
            outVoterPanel.TabIndex = this.panelVoter.TabIndex;
            outVoterPanel.Visible = true;
            // 
            // pbVoter
            // 
            PictureBox pbV = new PictureBox();
            outVoterPanel.Controls.Add(pbV);
            pbV.Location = this.pbVoter.Location;
            pbV.Name = this.pbVoter.Name + "_" + info.m_sID;
            pbV.Size = this.pbVoter.Size;
            pbV.TabIndex = this.pbVoter.TabIndex;
            pbV.TabStop = this.pbVoter.TabStop;
            pbV.Image = info.m_image;
            // 
            // btnCancelVote
            // 
            Button btnCancel = new Button();
            outVoterPanel.Controls.Add(btnCancel);
            btnCancel.Location = this.btnCancelVote.Location;
            btnCancel.Name = this.btnCancelVote.Name + "_" + info.m_sID;
            btnCancel.Size = this.btnCancelVote.Size;
            btnCancel.TabIndex = this.btnCancelVote.TabIndex;
            btnCancel.Text = this.btnCancelVote.Text;
            btnCancel.UseVisualStyleBackColor = this.btnCancelVote.UseVisualStyleBackColor;
            btnCancel.Click += (sender, e) => CancelVoting(Int64.Parse(info.m_sID));
            // 
            // btnCompleteVote
            // 
            Button btnCompleteVote = new Button();
            outVoterPanel.Controls.Add(btnCompleteVote);
            btnCompleteVote.Location = this.btnCompleteVote.Location;
            btnCompleteVote.Name = this.btnCompleteVote.Name + "_" + info.m_sID;
            btnCompleteVote.Size = this.btnCompleteVote.Size;
            btnCompleteVote.TabIndex = this.btnCompleteVote.TabIndex;
            btnCompleteVote.Text = this.btnCompleteVote.Text;
            btnCompleteVote.UseVisualStyleBackColor = this.btnCompleteVote.UseVisualStyleBackColor;
            btnCompleteVote.Click += (sender, e) => CompleteVoting(Int64.Parse(info.m_sID));
            // 
            // labelVoterID
            // 
            Label labelID = new Label();
            outVoterPanel.Controls.Add(labelID);
            labelID.AutoSize = this.labelVoterID.AutoSize;
            labelID.Location = this.labelVoterID.Location;
            labelID.Name = this.labelVoterID.Name + "_" + info.m_sID;
            labelID.Size = this.labelVoterID.Size;
            labelID.TabIndex = this.labelVoterID.TabIndex;
            labelID.Text = info.m_sID;
            // 
            // labelVoterName
            //
            Label labelName = new Label();
            outVoterPanel.Controls.Add(labelName);
            labelName.AutoSize = this.labelVoterName.AutoSize;
            labelName.Location = this.labelVoterName.Location;
            labelName.Name = this.labelVoterName.Name + "_" + info.m_sID;
            labelName.Size = this.labelVoterName.Size;
            labelName.TabIndex = this.labelVoterName.TabIndex;
            labelName.Text = info.m_sName;
            // 
            // labelVoterHName
            //
            Label labelHName = new Label();
            outVoterPanel.Controls.Add(labelHName);
            labelHName.AutoSize = this.labelVoterHName.AutoSize;
            labelHName.Location = this.labelVoterHName.Location;
            labelHName.Name = this.labelVoterHName.Name + "_" + info.m_sID;
            labelHName.Size = this.labelVoterHName.Size;
            labelHName.TabIndex = this.labelVoterHName.TabIndex;
            labelHName.Text = this.labelVoterHName.Text;
            // 
            // labelVoterHID
            // 
            Label labelHID = new Label();
            outVoterPanel.Controls.Add(labelHID);
            labelHID.AutoSize = this.labelVoterHID.AutoSize;
            labelHID.Location = this.labelVoterHID.Location;
            labelHID.Name = this.labelVoterHID.Name + "_" + info.m_sID;
            labelHID.Size = this.labelVoterHID.Size;
            labelHID.TabIndex = this.labelVoterHID.TabIndex;
            labelHID.Text = this.labelVoterHID.Text;
        }

        private void btnSearch_Click(object sender, EventArgs e)
        {
            long lID = 0;
            try
            {
                lID = Int64.Parse(tbIdSearch.Text);
            }
            catch (Exception exc)
            {
                MessageBox.Show("Error: Illegal ID the ID must be a number", "Error Illegal ID");
                return;
            }
            currentSearchedVoter = allVotersDB.GetCopyVoter(lID);
            if (currentSearchedVoter != null)
            {
                labelID.Text = currentSearchedVoter.lID.ToString();
                labelName.Text = currentSearchedVoter.sName;
                labelStatus.Text = currentSearchedVoter.Status.ToString();
                if (currentSearchedVoter.Status == VoteStatus.FreeToVote)
                {
                    btnRequestStartVoting.Enabled = true;
                }
            }
            else
            {
                labelID.Text = "Doesn't Exists";
                labelName.Text = "Doesn't Exists";
                labelStatus.Text = "Doesn't Exists";
                btnRequestStartVoting.Enabled = false;
            }
        }

        private void btnRequestStartVoting_Click(object sender, EventArgs e)
        {
            string sError;
            bool bHasSucceed = allVotersDB.RequestStartVoting(currentSearchedVoter, out sError);
            if (bHasSucceed)
            {
                AddVoterToCurrentVoters(currentSearchedVoter);
                currentSearchedVoter = null;
                labelID.Text = "";
                labelName.Text = "";
                labelStatus.Text = "";
                btnRequestStartVoting.Enabled = false;
            }
            else
            {
                MessageBox.Show("Voting request failed: " + sError, "Voting Request Failure");
            }
        }

        private void AddVoterToCurrentVoters(Voter voterToVote)
        {
            Array.Resize(ref AllCurrentVoters, AllCurrentVoters.Length + 1);
            Array.Resize(ref AllCurrentVotersPanels, AllCurrentVotersPanels.Length + 1);
            AllCurrentVoters[AllCurrentVoters.Length - 1] = voterToVote;
            CreateVoterCopy(ref AllCurrentVotersPanels[AllCurrentVotersPanels.Length - 1], GetPersonalInfoFromVoter(voterToVote));
            ArrangeAllCurrentVoters();
        }

        private void ArrangeAllCurrentVoters()
        {
            int BaseY = 20;
            int AdditionalY = 140;
            for (int i = 0; i < AllCurrentVotersPanels.Length; i++)
            {
                AllCurrentVotersPanels[i].Location = new Point(AllCurrentVotersPanels[i].Location.X, BaseY + i * AdditionalY);
            }
        }

        void RemoveVoter(int ind)
        {
            panelVoters.Controls.Remove(AllCurrentVotersPanels[ind]);
            for (int a = ind; a < AllCurrentVotersPanels.Length - 1; a++)
            {
                // moving elements downwards, to fill the gap at [index]
                AllCurrentVotersPanels[a] = AllCurrentVotersPanels[a + 1];
                AllCurrentVoters[a] = AllCurrentVoters[a + 1];
            }
            // finally, let's decrement Array's size by one
            Array.Resize(ref AllCurrentVotersPanels, AllCurrentVotersPanels.Length - 1);
            Array.Resize(ref AllCurrentVoters, AllCurrentVoters.Length - 1);
        }
        private void CancelVoting(long lId)
        {
            int ind = GetIndexFromID(lId);
            allVotersDB.CancelVoting(AllCurrentVoters[ind]);
            RemoveVoter(ind);
            ArrangeAllCurrentVoters();
        }

        private void CompleteVoting(long lId)
        {
            string sVote = "";
            try
            {
                OpenFileDialog ofd = new OpenFileDialog();
                DialogResult dr = ofd.ShowDialog();
                if(dr != System.Windows.Forms.DialogResult.OK)
                {
                    return;
                }
                StreamReader sr = new StreamReader(ofd.OpenFile());
                sVote = sr.ReadToEnd();
            }
            catch(Exception e)
            {
                MessageBox.Show("Failed completing vote! " + e.ToString());
                return;
            }
            if(sVote == "")
            {
                MessageBox.Show("Failed completing vote! vote is empty!");
                return;
            }
            int ind = GetIndexFromID(lId);
            allVotersDB.ConfirmVoting(AllCurrentVoters[ind], sVote);
            RemoveVoter(ind);
            ArrangeAllCurrentVoters();
        }

        private void FormMain_FormClosing(object sender, FormClosingEventArgs e)
        {
            if(serverCom != null)
            {
                serverCom.CloseConnection();
            }
        }

        private int GetIndexFromID(long lId)
        {
            for (int i = 0; i < AllCurrentVoters.Length; i++)
            {
                if(AllCurrentVoters[i].lID == lId)
                {
                    return i;
                }
            }
            return -1;
        }

        private void FormMain_FormClosed(object sender, FormClosedEventArgs e)
        {
            if (serverCom != null)
            {
                serverCom.CloseConnection();
                serverCom = null;
            }
        }

        private void FormMain_Load(object sender, EventArgs e)
        {
            m_bIsLogIn = false;
            m_LoginForm = new LogOnForm(Connect, Reconnect);
            m_LoginForm.ShowDialog();
            if (m_bIsLogIn == false)
            {
                Close();
            }
        }

        private void reloadVotersToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if(serverCom != null)
            {
                string sError;
                bool bRes = serverCom.GetVoterListFromServer(out sError);
                if(bRes == false)
                {
                    MessageBox.Show("Error loading voters. " + sError, "Failure Reloading Voters");
                }
            }
        }

    }
}
