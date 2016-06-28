/*
 * serial_client.c
 *
 * Use to test AT Command
 */

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h> /* close */
#include <pthread.h>
#include <termios.h>
#include <fcntl.h>
#include <string.h>
#include <stdbool.h>
#include <stdint.h>
#define LOG_TAG "ATCMD"
#include <cutils/log.h>
#include <cutils/properties.h>

#define BASEBAND_PROP "ro.baseband"
#define MSM_BASEBAND "msm"
#define MDM_BASEBAND "mdm"

#define DATABITS       CS8
#define BAUD            B115200
#define STOPBITS        0
#define PARITYON        0
#define PARITY          0
#define SERIAL_PORT_INTEL      "/dev/gsmtty19"
#define SERIAL_PORT_MSM        "/dev/smd7"
#define SERIAL_PORT_MDM        "/dev/gdun0"
#define SSR_CMD "AT+CUSD=1,##3424*9#"
#define SMS_CMD1 "AT+CMGS="
#define SMS_CMD2 "AT+CMGW="
static char acDeviceName1[PATH_MAX];
#include <errno.h>
#define BUFFSIZE    1024
static char s_atCommand[BUFFSIZE];
static bool s_isWait = true;

static pthread_mutex_t atcommand_mutex;
static pthread_cond_t atcommand_signal;

static int serialfd1;

#define LOGI(...) do {\
    ALOGI(__VA_ARGS__);\
    printf(__VA_ARGS__);\
} while(0)

#define LOGE(...) do {\
    ALOGE(__VA_ARGS__);\
    printf(__VA_ARGS__);\
} while(0)

#define SERVER_PORT 1500
#define MAX_MSG 100

#define UNUSED(a)       ((void)(a))

void *ReceiveDataFromChan1(void* parm);

void *ReceiveDataFromChan1(void* parm)
{
    UNUSED(parm);

    int rc;
    rc = pthread_detach(pthread_self());
    if (rc != 0) {
        LOGE("pthread_detach\n");
    }
    /*
     *  check if there is response
     */
    char buffer[BUFFSIZE];
    int bytes = 0;
    while (1) {

        bytes = read(serialfd1, buffer, BUFFSIZE - 1);
        if (bytes < 0) {
            LOGE("read %d error %s\n", serialfd1, strerror(errno));
            break;
        } else if (bytes == 0) {
            LOGI("read %d zero bytes\n", serialfd1);
            pthread_mutex_lock(&atcommand_mutex);
            pthread_cond_signal(&atcommand_signal);
            pthread_mutex_unlock(&atcommand_mutex);
            break;
        }
        buffer[bytes] = '\0';        /* Assure null terminated string */

        LOGI("%s", buffer);
        if (s_atCommand[0] != 0) {
            // Wait Send AT Command reponse
            if (strcasestr(buffer, "OK") ||
                    strcasestr(buffer, "ERROR")) {
                pthread_mutex_lock(&atcommand_mutex);
                pthread_cond_signal(&atcommand_signal);
                pthread_mutex_unlock(&atcommand_mutex);
                break;
            }
        }
    }
    return 0;
}

static void HelpInfo()
{
    LOGI( "Usage: serial_client [-c \"AT command\"] [-h]\n"
            "-c: Send an at command to atcmdsrv\n"
            "-w: Send an at command to atcmdsrv no wait\n"
            "-z: append ctrl+z to an at command(sms)\n"
            "-h: Print help info\n");
}

static int StringProcess(char *pString)
{
    int len = 0;
    char tmpString[BUFFSIZE];
    len = strlcpy(tmpString, pString, sizeof(tmpString)/sizeof(tmpString[0]));
    if (len <= 0) {
        LOGI("copy %s to atCommand error\n", pString);
        return -1;
    }
    int i;
    int j = 0;
    for (i = 0; i < len; ++i) {
        if (tmpString[i] != '\\' ) {
            s_atCommand[j++] = tmpString[i];
        } else {
            switch (tmpString[++i]) {
            case 'x': {
                char num[3];
                memset(num, 0, sizeof(num));
                memcpy(num, tmpString+(i+1), 2);
                uint8_t bin = (uint8_t)strtoul(num, NULL, 16);
                s_atCommand[j++] = bin;
                i += 2;
            }
            break;
            case 'r':
                s_atCommand[j++] = '\r';
                break;
            default:
                LOGI("error unknow \\%c\n", tmpString[i]);
                return -1;
            }
        }
    }
    return 0;
}


static int GetArgs(int argc, char** argv)
{
    int i;
    int len = 0;

    // Start at i = 1 to skip the command name.
    for (i = 1; i < argc; ++i) {
        // Check for a switch (leading "-").
        if (argv[i][0] == '-') {
            // Use the next character to decide what to do
            switch (argv[i][1]) {
            case 'c':
                len = StringProcess(argv[++i]);
                if (len < 0) {
                    LOGI("StringProcess error");
                    return len;
                }
                strlcat(s_atCommand, "\r", sizeof(s_atCommand)/sizeof(s_atCommand[0]));
                break;
            case 'z':
                len = StringProcess(argv[++i]);
                if (len < 0) {
                    LOGI("StringProcess error");
                    return len;
                }
                strlcat(s_atCommand, "\x1a\r", sizeof(s_atCommand)/sizeof(s_atCommand[0]));
                break;
            case 'w':
                len = StringProcess(argv[++i]);
                if (len < 0) {
                    LOGI("StringProcess error");
                    return len;
                }
                strlcat(s_atCommand, "\r", sizeof(s_atCommand)/sizeof(s_atCommand[0]));
                s_isWait = false;
                break;
            case 'h':
                HelpInfo();
                return 1;
            default:
                LOGI("Unknown args %s\n", argv[i]);
                return -1;
            }
        } else {
            len = strlcpy(acDeviceName1, argv[i], sizeof(acDeviceName1)/sizeof(acDeviceName1[0]));
            if (len <= 0) {
                LOGI("copy %s to DeviceName error\n", argv[i]);
                return -1;
            }
        }
    }
    return 0;
}

int main (int argc, char *argv[])
{

    int rc;
    int recieved;

    /*
     * if there are the right number of parameters
     */
    memset(s_atCommand, 0, sizeof(s_atCommand));
    memset(acDeviceName1, 0, sizeof(acDeviceName1));


    if (argc > 1) {
        int ret;

        ret = GetArgs(argc, argv);
        if (ret == -1) {
            LOGI("Error to handle argv\n");
            return -1;
        } else if (ret == 1)
            return 0;
    }

    struct termios oldtio, newtio;       //place for old and new port settings for serial port

    /*
     *  Open the Device
     */

    if (strlen(acDeviceName1) != 0) {
        /*open the device(com port) to be non-blocking (read will return immediately) */
        serialfd1 = open(acDeviceName1, O_RDWR);
        if (serialfd1 <= 0) {
            LOGE("opening %s error %s\n", acDeviceName1, strerror(errno));
            return 0;
        } else
            LOGI("Device : %s\n", acDeviceName1);
    } else {
        char args[PROPERTY_VALUE_MAX];
        char open_port[PATH_MAX];
        property_get(BASEBAND_PROP, args, MSM_BASEBAND);
        if (!strncasecmp(args, MSM_BASEBAND, strlen(MSM_BASEBAND)))
            strlcpy(open_port, SERIAL_PORT_MSM, sizeof(open_port));
        else if (!strncasecmp(args, MDM_BASEBAND, strlen(MDM_BASEBAND)))
            strlcpy(open_port, SERIAL_PORT_MDM, sizeof(open_port));
        else
            strlcpy(open_port, SERIAL_PORT_INTEL, sizeof(open_port));
        serialfd1 = open(open_port, O_RDWR);
        if (serialfd1 <= 0) {
            LOGE("opening %s error %s\n", open_port, strerror(errno));
            return 0;
        } else
            printf("Device : %s\n", open_port);
    }

    // Make the file descriptor asynchronous (the manual page says only
    // O_APPEND and O_NONBLOCK, will work with F_SETFL...)

    fcntl(serialfd1, F_SETFL, 0);

    tcgetattr(serialfd1, &oldtio); // save current port settings

    // set new port settings for canonical input processing
    newtio.c_cflag = BAUD | CRTSCTS | DATABITS | STOPBITS | PARITYON | PARITY | CLOCAL | CREAD;
    newtio.c_iflag = IGNPAR;
    newtio.c_oflag = 0;
    ///newtio.c_lflag =  ECHOE | ECHO | ICANON;       //ICANON;
    newtio.c_lflag =  0;       //ICANON;

    newtio.c_cc[VMIN] = 1;
    newtio.c_cc[VTIME] = 0;
    newtio.c_cc[VERASE] = 0x8;
    newtio.c_cc[VEOL] = 0xD;

    newtio.c_cc[VINTR]    = 0;      /* Ctrl-c */
    newtio.c_cc[VQUIT]    = 0;      /* Ctrl-\ */
    newtio.c_cc[VERASE]   = 0;      /* del */
    newtio.c_cc[VKILL]    = 0;      /* @ */
    newtio.c_cc[VEOF]     = 4;      /* Ctrl-d */
    newtio.c_cc[VTIME]    = 0;      /* inter-character timer unused */
    newtio.c_cc[VMIN]     = 1;      /* blocking read until 1 character arrives */
    newtio.c_cc[VSWTC]    = 0;      /* '\0' */
    newtio.c_cc[VSTART]   = 0;      /* Ctrl-q */
    newtio.c_cc[VSTOP]    = 0;      /* Ctrl-s */
    newtio.c_cc[VSUSP]    = 0;      /* Ctrl-z */
    newtio.c_cc[VEOL]     = 0;      /* '\0' */
    newtio.c_cc[VREPRINT] = 0;      /* Ctrl-r */
    newtio.c_cc[VDISCARD] = 0;      /* Ctrl-u */
    newtio.c_cc[VWERASE]  = 0;      /* Ctrl-w */
    newtio.c_cc[VLNEXT]   = 0;      /* Ctrl-v */
    newtio.c_cc[VEOL2]    = 0;      /* '\0' */

    newtio.c_cc[VMIN] = 1;
    newtio.c_cc[VTIME] = 0;
    newtio.c_cc[VERASE] = 0x8;
    newtio.c_cc[VEOL] = 0xD;

    tcflush(serialfd1, TCIFLUSH);
    tcsetattr(serialfd1, TCSANOW, &newtio);

    /*
     *  Create a thread to recieve
     */

    pthread_t InputThread1;
    if ( pthread_create( &InputThread1, NULL, (void *)ReceiveDataFromChan1, NULL) != 0 ) {
        LOGE("pthread_create");
        exit( 1 );
    }

    if (s_atCommand[0] != 0) {
        struct timespec timeout;
        int write_retry = 20;

        pthread_mutex_init(&atcommand_mutex, NULL);
        pthread_cond_init (&atcommand_signal, NULL);

        pthread_mutex_lock(&atcommand_mutex);
        LOGI("S: %s\n", s_atCommand);
        while (write_retry > 0) {
            rc = write(serialfd1, s_atCommand, strlen(s_atCommand));
            if (rc < 0 && --write_retry > 0) {
                struct timespec sleep_time = {0,100000000}; // 100ms
                nanosleep(&sleep_time, NULL);
            } else {
                break;
            }
        }
        if (rc < 0)
            LOGE("cannot send data ");
        else {
            if (!strncasecmp(s_atCommand, SSR_CMD, strlen(SSR_CMD))) {
                struct timespec sleep_time = {5,0}; // 5s
                nanosleep(&sleep_time, NULL);

                LOGI("\nTrigger SSR\n");
            } else if (!strncasecmp(s_atCommand, SMS_CMD1, strlen(SMS_CMD1)) ||
                       !strncasecmp(s_atCommand, SMS_CMD2, strlen(SMS_CMD2))) {
                struct timespec sleep_time = {1,0}; // 1s
                nanosleep(&sleep_time, NULL);

                LOGI("\nSEND SMS MESSAGE\n");
            } else if (s_isWait) {
                clock_gettime(CLOCK_REALTIME, &timeout);
                timeout.tv_sec += 60;
                rc = pthread_cond_timedwait(&atcommand_signal, &atcommand_mutex, &timeout);
                if (rc == ETIMEDOUT) {
                    LOGI("Timeout %s\n", s_atCommand);
                }
            } else {
                struct timespec sleep_time = {1,0}; // 1s
                nanosleep(&sleep_time, NULL);
            }
        }
        pthread_mutex_unlock(&atcommand_mutex);

        pthread_mutex_destroy(&atcommand_mutex);
        pthread_cond_destroy(&atcommand_signal);
        close(serialfd1);
    } else {
        unsigned char buf[BUFFSIZE];

        while (1) { /* loop forever */

            recieved = read(0, buf, sizeof(buf));

            if (buf[recieved - 1] == '\a' || buf[recieved - 1] == 0xa ) {
                buf[recieved - 1] = 0xd;
            }

            rc = write(serialfd1, buf, recieved);

            if (rc < 0) {
                LOGE("cannot send data ");
            }

        } /* end while */
    }
    return 0;

}
