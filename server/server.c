#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

#define PORT 5000
#define BUFFER_SIZE 16
#define RESPONSE_SIZE 4

int main() {
    int sockfd;
    struct sockaddr_in serv_addr, cli_addr;
    socklen_t cli_len = sizeof(cli_addr);
    char buffer[BUFFER_SIZE];
    char response[RESPONSE_SIZE];

    // Create UDP socket
    if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("socket creation failed");
        exit(EXIT_FAILURE);
    }

    memset(&serv_addr, 0, sizeof(serv_addr));
    memset(&cli_addr, 0, sizeof(cli_addr));

    // Configure server address
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);

    // Bind socket to address
    if (bind(sockfd, (const struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
        perror("bind failed");
        close(sockfd);
        exit(EXIT_FAILURE);
    }

    printf("UDP Server listening on port %d...\n", PORT);

    while (1) {
        // Receive UDP packet
        int n = recvfrom(sockfd, buffer, BUFFER_SIZE, 0,
                         (struct sockaddr *) &cli_addr, &cli_len);
        if (n < 0) {
            perror("recvfrom failed");
            continue;
        }

        // Null-terminate the string
        buffer[n] = '\0';

        // Get client IP
        char client_ip[INET_ADDRSTRLEN];
        inet_ntop(AF_INET, &cli_addr.sin_addr, client_ip, INET_ADDRSTRLEN);

        // Display received string
        printf("Received from %s:%d - '%s' (%d chars)\n",
               client_ip, ntohs(cli_addr.sin_port), buffer, n);

        // Format character count as string
        int len = snprintf(response, RESPONSE_SIZE, "%d", n);
        if (len < 0 || len >= RESPONSE_SIZE) {
            fprintf(stderr, "Error formatting response\n");
            continue;
        }

        // Send character count back to client
        if (sendto(sockfd, response, len, 0,
                   (struct sockaddr *) &cli_addr, cli_len) < 0) {
            perror("sendto failed");
        }
    }

    close(sockfd);
    return 0;
}
