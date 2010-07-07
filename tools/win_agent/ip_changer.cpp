#include <iostream>
#include <string>


#include <winsock2.h>
#include <windows.h>
#include "iphlpapi.h"

using namespace std;

BOOL RegSetIP(LPCTSTR lpszAdapterName, LPCTSTR pIPAddress, LPCTSTR pNetMask, LPCTSTR pNetGate, LPCTSTR pNameServer)
{
	HKEY hKey;
	wstring strKeyName = L"SYSTEM\\CurrentControlSet\\Services\\Tcpip\\Parameters\\Interfaces\\";
	strKeyName += lpszAdapterName;
	
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, strKeyName.c_str(), 0, KEY_WRITE, &hKey) != ERROR_SUCCESS)
		return FALSE;
	
	wchar_t mszIPAddress[100];
	wchar_t mszNetMask[100];
	wchar_t mszNetGate[100];
	wchar_t mszNameServer[100];

	memset(mszIPAddress, 0, sizeof(wchar_t) * 100);
	memset(mszNetMask, 0,  sizeof(wchar_t) * 100);
	memset(mszNetGate, 0, sizeof(wchar_t) * 100);
	memset(mszNameServer, 0, sizeof(wchar_t) * 100);

	wcsncpy(mszIPAddress, pIPAddress, 98);
	wcsncpy(mszNetMask, pNetMask, 98);
	wcsncpy(mszNetGate, pNetGate, 98);
	wcsncpy(mszNameServer, pNameServer, 98);

	int nIP, nMask, nGate, nNameServer;

	nIP = wcslen(mszIPAddress);
	nMask = wcslen(mszNetMask);
	nGate = wcslen(mszNetGate);
	nNameServer = wcslen(mszNameServer);

	*(mszIPAddress + nIP + 1) = 0x00; 
	nIP += 2;
	*(mszNetMask + nMask + 1) = 0x00;
	nMask += 2;
	*(mszNetGate + nGate + 1) = 0x00;
	nGate += 2;
	*(mszNameServer + nNameServer + 1) = 0x00;
	nNameServer += 2;
	
	DWORD DHCP = 0;
	RegSetValueEx(hKey, L"EnableDHCP", 0, REG_DWORD, (BYTE *)(&DHCP), sizeof(DWORD));
	RegSetValueEx(hKey, L"IPAddress", 0, REG_MULTI_SZ, (BYTE *)mszIPAddress, sizeof(wchar_t) * nIP);
	RegSetValueEx(hKey, L"SubnetMask", 0, REG_MULTI_SZ, (BYTE *)mszNetMask, sizeof(wchar_t) * nMask);
	RegSetValueEx(hKey, L"DefaultGateway", 0, REG_MULTI_SZ, (BYTE *)mszNetGate, sizeof(wchar_t) * nGate);
	RegSetValueEx(hKey, L"NameServer", 0, REG_SZ, (BYTE *)mszNameServer, sizeof(wchar_t) * nNameServer);
	RegCloseKey(hKey);
	return TRUE;
}


int main(int argc, char *argv[]) {
	if (argc <= 4) {
		printf("usage: %s <IP Address> <NetMask> <Default Gateway> <DNS Server>\n", argv[0]);
		return 1;
	}
	wchar_t IPAddr[100];
	wchar_t NetMask[100];
	wchar_t Gateway[100];
	wchar_t DNS[100];

	memset(IPAddr, 0, sizeof(wchar_t) * 100);
	memset(NetMask, 0, sizeof(wchar_t) * 100);
	memset(Gateway, 0, sizeof(wchar_t) * 100);
	memset(DNS, 0, sizeof(wchar_t) * 100);

	if (argc > 1)
		MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, argv[1], strlen(argv[1]), IPAddr, 100);
	if (argc > 2)
		MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, argv[2], strlen(argv[2]), NetMask, 100);
	if (argc > 3)
		MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, argv[3], strlen(argv[3]), Gateway, 100);
	if (argc > 4)
		MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, argv[4], strlen(argv[4]), DNS, 100);

	//wcout << IPAddr << endl;
	//wcout << NetMask << endl;
	//wcout << Gateway << endl;
	//wcout << DNS << endl;

	//return 0;
	HINSTANCE hInst = NULL;
	HINSTANCE hDhcp = NULL;
	
	hInst = LoadLibrary(L"iphlpapi.dll");
	if (!hInst) {
		cout << "iphlpapi.dll not supported in this platform!\n";
		return 1;
	}

	hDhcp = LoadLibrary(L"dhcpcsvc.dll");
	if (!hDhcp) {
		cout << "dhcpcsvc.dll not supported in this platform!\n";
		return 1;
	}

	typedef DWORD(CALLBACK *PGAINFO) (PIP_ADAPTER_INFO, PULONG);
	typedef DWORD(CALLBACK *PDNCC) (LPWSTR, LPWSTR, BOOL, DWORD, DWORD, DWORD, int);

	PGAINFO pGAInfo = (PGAINFO) GetProcAddress(hInst, "GetAdaptersInfo");
	PDNCC pDhcpNotifyProc = (PDNCC) GetProcAddress(hDhcp, "DhcpNotifyConfigChange");

	PIP_ADAPTER_INFO pInfo=NULL;
	ULONG ulSize=0;

	pGAInfo(pInfo, &ulSize);
	pInfo=(PIP_ADAPTER_INFO) (new char[ulSize]);
	pGAInfo(pInfo, &ulSize);

	//bool regResult = false;
	wchar_t *wAdapterName = new wchar_t[260];
	memset(wAdapterName, 0, sizeof(wchar_t) * 260);

	while (pInfo) {
		//cout << pInfo->AdapterName << endl;
		
		MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, pInfo->AdapterName, 260, wAdapterName, 260);

		//wcout << wAdapterName << endl;
		//break;
		RegSetIP(wAdapterName, IPAddr, NetMask, Gateway, DNS);
		break;

		//pInfo=pInfo->Next; 
	}



//if((pDhcpNotifyProc = (DHCPNOTIFYPROC)GetProcAddress(hDhcpDll, "DhcpNotifyConfigChange")) != NULL)
	//if((pDhcpNotifyProc)(NULL, wcAdapterName, TRUE, nIndex, inet_addr(pIPAddress), inet_addr(pNetMask), 0) == ERROR_SUCCESS)
	//cout << argv[1] << endl << argv[2] << endl;
	//cout << inet_addr(argv[1]) << endl << inet_addr(argv[2]) << endl;
	pDhcpNotifyProc(NULL, wAdapterName, TRUE, 0, inet_addr(argv[1]), inet_addr(argv[2]), 0);

	FreeLibrary(hDhcp);
	FreeLibrary(hInst);
	return 0;
}