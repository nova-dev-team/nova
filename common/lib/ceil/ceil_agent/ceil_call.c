#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>


int main(int argc, char *argv[]) {
  int sockfd, portno;
  int n;
  struct sockaddr_in server_addr, client_addr;
  struct hostent *server;

  char buffer[256];
  portno = 32167;
  sockfd = socket(AF_INET, SOCK_STREAM, 0);

  server = gethostbyname("127.0.0.1");

  bzero((char *) &server_addr, sizeof(server_addr));
  server_addr.sin_family = AF_INET;
  bcopy((char *)server->h_addr, (char *) &server_addr.sin_addr.s_addr, server->h_length);
  server_addr.sin_port = htons(portno);
  connect(sockfd, &server_addr, sizeof(server_addr));
  bzero(buffer, 256);
  sprintf(buffer, "blah");
  write(sockfd, buffer, strlen(buffer));
  bzero(buffer, 256);
  read(sockfd, buffer, 255);
  printf("[MSG]%s\n", buffer);

  return 0;
}
