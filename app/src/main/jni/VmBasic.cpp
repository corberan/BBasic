//
// Created by liuzc on 16/4/26.
//

#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <cstring>
#include <cmath>
#include <cstdlib>
#include "itemplat.h"
#include "istring.h"
#include "time.h"

typedef unsigned int vdword;
typedef unsigned short vword;
typedef unsigned char vbyte;
typedef float vfloat;
typedef int vint;

vbyte *VirtualMem;
vdword Data_pointor;
ILister<iString> *VmStrings=new ILister<iString>;

struct VmReg
{
    vint  RP,RF,RS,RB,R0,R1,R2,R3;
}VmRegs;

vint *regs=&VmRegs.RP;
vdword VmSize;

jmethodID color;
jmethodID pixlocate;
jmethodID locate;
jmethodID font;
jmethodID intprint;
jmethodID strprint;
jmethodID floatprint;
jmethodID out4;
jmethodID cls;
jmethodID loadres;
jmethodID createpage;
jmethodID deletepage;
jmethodID showpic;
jmethodID freeres;
jmethodID flippage;
jmethodID fillpage;
jmethodID getpichgt;
jmethodID getpicwid;
jmethodID bitbltpage;
jmethodID stretchbltpage;
jmethodID stretchbltpageEx;
jmethodID pixel;
jmethodID readpixel;
jmethodID setbkmode;
jmethodID setpen;
jmethodID setbrush;
jmethodID moveto;
jmethodID lineto;
jmethodID rectangle;
jmethodID circle;
jmethodID openfile;
jmethodID putInt;
jmethodID getInt;
jmethodID putBytes;
jmethodID getBytes;
jmethodID putFloat;
jmethodID getFloat;
jmethodID eof;
jmethodID lof;
jmethodID loc;
jmethodID seek;
jmethodID closefile;
jmethodID waitkey;
jmethodID inkey;
jmethodID KeyPress;
jmethodID input_int;
jmethodID input_float;
jmethodID input_str;
jmethodID setlcd;
jmethodID gettick;

JNIEnv * jenv;
jobject jthiz;

vdword VmIn(vdword port);
void VmOut(vdword port,vdword value);

int isContinue = 1;
int tick = 1000;

extern "C" {
JNIEXPORT jint JNICALL Java_com_liuzc_bbasic_VirtualMachine_stopRunning
        (JNIEnv *, jobject);
JNIEXPORT jint JNICALL Java_com_liuzc_bbasic_VirtualMachine_runExecCode
        (JNIEnv *, jobject, jint, jbyteArray, jintArray, jint);
}

jint JNICALL Java_com_liuzc_bbasic_VirtualMachine_stopRunning(JNIEnv * env, jobject thiz){
    isContinue = 0;
}

jint JNICALL Java_com_liuzc_bbasic_VirtualMachine_runExecCode(JNIEnv * env, jobject thiz, jint jexecFileLength, jbyteArray jexecFileBytes, jintArray jRegs, jint jtime){
    jenv = env;
    jthiz = thiz;
    tick = (vint)jtime;

    vint *VmStartup = (vint *) env->GetIntArrayElements(jRegs,0);
    VirtualMem = (vbyte *) env->GetByteArrayElements(jexecFileBytes,0);

    jclass jcls = env->GetObjectClass(thiz);

    color = env->GetMethodID(jcls, "color", "(III)V");
    pixlocate = env->GetMethodID(jcls, "pixlocate", "(II)V");
    locate = env->GetMethodID(jcls, "locate", "(II)V");
    font = env->GetMethodID(jcls, "font", "(I)V");
    strprint = env->GetMethodID(jcls, "print", "([B)V");
    intprint = env->GetMethodID(jcls, "print", "(I)V");
    floatprint = env->GetMethodID(jcls, "print", "(F)V");
    out4 = env->GetMethodID(jcls, "out4", "(I)V");
    cls = env->GetMethodID(jcls, "cls", "()V");
    createpage = env->GetMethodID(jcls, "createpage", "()I");
    deletepage = env->GetMethodID(jcls, "deletepage", "(I)I");
    loadres = env->GetMethodID(jcls, "loadres", "([BI)I");
    freeres = env->GetMethodID(jcls, "freeres", "(I)V");
    showpic = env->GetMethodID(jcls, "showpic", "(IIIIIIIII)V");
    flippage = env->GetMethodID(jcls, "flippage", "(I)V");
    fillpage = env->GetMethodID(jcls, "fillpage", "(IIIIII)V");
    getpichgt = env->GetMethodID(jcls, "getpichgt", "(I)I");
    getpicwid = env->GetMethodID(jcls, "getpicwid", "(I)I");
    bitbltpage = env->GetMethodID(jcls, "bitbltpage", "(II)V");
    stretchbltpage = env->GetMethodID(jcls, "stretchbltpage", "(IIIIIIII)V");
    stretchbltpageEx = env->GetMethodID(jcls, "stretchbltpageEx", "(IIIIIIII)V");
    pixel = env->GetMethodID(jcls, "pixel", "(IIII)V");
    readpixel = env->GetMethodID(jcls, "readpixel", "(III)I");
    setbkmode = env->GetMethodID(jcls, "setbkmode", "(I)V");
    setpen = env->GetMethodID(jcls, "setpen", "(IIII)V");
    setbrush = env->GetMethodID(jcls, "setbrush", "(II)V");
    moveto = env->GetMethodID(jcls, "moveto", "(III)V");
    lineto = env->GetMethodID(jcls, "lineto", "(III)V");
    rectangle = env->GetMethodID(jcls, "rectangle", "(IIIII)V");
    circle = env->GetMethodID(jcls, "circle", "(IIII)V");
    openfile = env->GetMethodID(jcls, "openfile", "([BI)V");
    putInt = env->GetMethodID(jcls, "putInt", "(II)V");
    getInt = env->GetMethodID(jcls, "getInt", "(I)I");
    putBytes = env->GetMethodID(jcls, "putBytes", "(I[B)V");
    getBytes = env->GetMethodID(jcls, "getBytes", "(I)[B");
    putFloat = env->GetMethodID(jcls, "putFloat", "(IF)V");
    getFloat = env->GetMethodID(jcls, "getFloat", "(I)F");
    eof = env->GetMethodID(jcls, "eof", "(I)I");
    lof = env->GetMethodID(jcls, "lof", "(I)I");
    loc = env->GetMethodID(jcls, "loc", "(I)I");
    seek = env->GetMethodID(jcls, "seek", "(II)V");
    closefile = env->GetMethodID(jcls, "closefile", "(I)V");
    waitkey = env->GetMethodID(jcls, "waitkey", "()I");
    inkey = env->GetMethodID(jcls, "InKey", "()I");
    KeyPress = env->GetMethodID(jcls, "KeyPress", "(I)I");
    input_int = env->GetMethodID(jcls, "input_int", "()[B");
    input_float = env->GetMethodID(jcls, "input_float", "()[B");
    input_str = env->GetMethodID(jcls, "input_str", "()[B");
    setlcd = env->GetMethodID(jcls, "setlcd", "(II)V");
    gettick = env->GetMethodID(jcls, "gettick", "()I");

    vbyte *code = VirtualMem;
    VmSize = jexecFileLength;
    memcpy(&VmRegs,VmStartup,sizeof(VmRegs));
    vbyte opr;
    vbyte mode;
    vdword ip;
    vdword minsp=0;
    vdword maxsize=VmSize-1;
    vbyte  type1,type2;
    vbyte *data1,*data2;
    vbyte  oprlen=0;
    bool VmStatus=0;
    short i;

    isContinue = 1;

    // int opr2oprlen[16];
    // opr2oprlen[0]=1;
    // opr2oprlen[1]=10;
    // opr2oprlen[2]=5;
    // opr2oprlen[3]=5;
    // opr2oprlen[4]=10;
    // opr2oprlen[5]=10;
    // opr2oprlen[6]=5;
    // opr2oprlen[7]=6;
    // opr2oprlen[8]=5;
    // opr2oprlen[9]=5;
    // opr2oprlen[10]=10;
    // opr2oprlen[11]=10;
    // opr2oprlen[15]=1;

    VmStrings->Clear();
    VmStrings->Alloc();

    while(VmStatus==0)
    {
        ip=VmRegs.RP;
        if(isContinue==0) break;
        opr=code[ip]&0xf0;
        mode=code[ip]&0x0f;

        ///////////////

        ///////////////

        switch (opr)
        {
            case 0x00: oprlen=1; break;
            case 0x10:
                oprlen=10;
                type1=((code[ip+1]>>2)&3);	type2=(code[ip+1]&3);
                data1=code+ip+2;			data2=data1+4;
                if (type1==0) data1=(vbyte*)&(regs[*data1]);
                else if (type1==1) data1=code+regs[*data1];
                else if (type1==2) break;
                else if (type1==3) data1=code+*(vdword*)data1;
                if (type2==0) data2=(vbyte*)&(regs[*data2]);
                else if (type2==1) data2=code+regs[*data2];
                else if (type2==3) data2=code+*(vdword*)data2;
                if (mode==2) *(vbyte*)data1=*(vbyte*)data2;
                else if (mode==1) *(vword*)data1=*(vword*)data2;
                else *(vdword*)data1=*(vdword*)data2;
                break;
            case 0x20:
                oprlen=5;
                data1=code+ip+1;
                if (mode==0x0f) {
                    for (i=0;i<8;i++) {
                        if (VmRegs.RS<minsp||VmRegs.RS>=maxsize) break;
                        *(vdword*)(code+VmRegs.RS)=regs[i];
                        VmRegs.RS-=4;
                    }
                } else {
                    if (VmRegs.RS<minsp||VmRegs.RS>=maxsize) break;
                    if (mode==0) data1=(vbyte*)&(regs[*data1]);
                    else if (mode==1) data1=code+regs[*data1];
                    else if (mode==3) data1=code+*(vdword*)data1;
                    *(vdword*)(code+VmRegs.RS)=*(vdword*)data1;
                    VmRegs.RS-=4;
                }
                break;
            case 0x30:
                oprlen=5;
                data1=code+ip+1;
                if (mode==0x0f) {
                    for (i=7;i>=0;i--) {
                        VmRegs.RS+=4;
                        if (VmRegs.RS>=maxsize) break;
                        regs[i]=*(vdword*)(code+VmRegs.RS);
                    }
                } else {
                    VmRegs.RS+=4;
                    if (VmRegs.RS>=maxsize) break;
                    if (mode==0) data1=(vbyte*)&(regs[*data1]);
                    else if (mode==1) data1=code+regs[*data1];
                    else if (mode==2) break;
                    else if (mode==3) data1=code+*(vdword*)data1;
                    *(vdword*)data1=*(vdword*)(code+VmRegs.RS);
                }
                break;
            case 0x40:
                oprlen=10;
                type1=((code[ip+1]>>2)&3);	type2=(code[ip+1]&3);
                data1=code+ip+2;			data2=data1+4;
                if (type1==0) data1=(vbyte*)&(regs[*data1]);
                else if (type1==1) data1=code+regs[*data1];
                else if (type1==2) break;
                else if (type1==3) data1=code+*(vdword*)data1;
                if (type2==0) data2=(vbyte*)&(regs[*data2]);
                else if (type2==1) data2=code+regs[*data2];
                else if (type2==3) data2=code+*(vdword*)data2;
                *(vdword*)data1=VmIn(*(vdword*)data2);
                break;
            case 0x50:
                oprlen=10;
                type1=((code[ip+1]>>2)&3);	type2=(code[ip+1]&3);
                data1=code+ip+2;			data2=data1+4;
                if (type1==0) data1=(vbyte*)&(regs[*data1]);
                else if (type1==1) data1=code+regs[*data1];
                else if (type1==3) data1=code+*(vdword*)data1;
                if (type2==0) data2=(vbyte*)&(regs[*data2]);
                else if (type2==1) data2=code+regs[*data2];
                else if (type2==3) data2=code+*(vdword*)data2;
                VmOut(*(vdword*)data1,*(vdword*)data2);
                break;
            case 0x60:
                oprlen=5;
                data1=code+ip+1;
                if (mode==0) data1=(vbyte*)&(regs[*data1]);
                else if (mode==1) data1=code+regs[*data1];
                else if (mode==3) data1=code+*(vdword*)data1;
                VmRegs.RP=*(vdword*)data1;
                if (VmRegs.RP>=maxsize) break;
                oprlen=0;
                break;
            case 0x70:
                oprlen=6;
                data1=code+ip+2;
                if (code[ip+1]==0) data1=(vbyte*)&(regs[*data1]);
                else if (code[ip+1]==1) data1=code+regs[*data1];
                else if (code[ip+1]==3) data1=code+*(vdword*)data1;
                if (VmRegs.RF&(code[ip]&7)) VmRegs.RP=*(vdword*)data1, oprlen=0;
                if (VmRegs.RP>=maxsize) break;
                break;
            case 0x80:
                oprlen=5;
                data1=code+ip+1;
                if (mode==0) data1=(vbyte*)&(regs[*data1]);
                else if (mode==1) data1=code+regs[*data1];
                else if (mode==3) data1=code+*(vdword*)data1;
                *(vdword*)&code[VmRegs.RS]=VmRegs.RP+oprlen;
                VmRegs.RS-=4;
                if (VmRegs.RS<minsp||VmRegs.RS>=maxsize) break;
                VmRegs.RP=*(vdword*)data1;
                if (VmRegs.RP>=maxsize) break;
                oprlen=0;
                break;
            case 0x90:
                oprlen=5;
                VmRegs.RS+=4;
                if (VmRegs.RS>=maxsize) break;
                VmRegs.RP=*(vdword*)&code[VmRegs.RS];
                if (VmRegs.RP>=maxsize) break;
                oprlen=0;
                break;
            case 0xA0:
                oprlen=10;
                i=0;
                type1=((code[ip+1]>>2)&3);	type2=(code[ip+1]&3);
                data1=code+ip+2;			data2=data1+4;
                if (type1==0) data1=(vbyte*)&(regs[*data1]);
                else if (type1==1) data1=code+regs[*data1];
                else if (type1==3) data1=code+*(vdword*)data1;
                if (type2==0) data2=(vbyte*)&(regs[*data2]);
                else if (type2==1) data2=code+regs[*data2];
                else if (type2==3) data2=code+*(vdword*)data2;
                if (mode==0) {
                    if (*(vdword*)data1==*(vdword*)data2) i=1;
                    if (*(vdword*)data1>*(vdword*)data2) i+=4;
                    if (*(vdword*)data1<*(vdword*)data2) i+=2;
                } else if (mode==1) {
                    if (*(vword*)data1==*(vword*)data2) i=1;
                    if (*(vword*)data1>*(vword*)data2) i+=4;
                    if (*(vword*)data1<*(vword*)data2) i+=2;
                } else if (mode==2) {
                    if (*(vbyte*)data1==*(vbyte*)data2) i=1;
                    if (*(vbyte*)data1>*(vbyte*)data2) i+=4;
                    if (*(vbyte*)data1<*(vbyte*)data2) i+=2;
                } else if (mode==3) {
                    if (*(vfloat*)data1==*(vfloat*)data2) i=1;
                    if (*(vfloat*)data1>*(vfloat*)data2) i+=4;
                    if (*(vfloat*)data1<*(vfloat*)data2) i+=2;
                } else if (mode==4) {
                    if (*(vint*)data1==*(vint*)data2) i=1;
                    if (*(vint*)data1>*(vint*)data2) i+=4;
                    if (*(vint*)data1<*(vint*)data2) i+=2;
                }
                VmRegs.RF=(VmRegs.RF&(0xffffffff-7))|i;
                break;
            case 0xB0:
                oprlen=10;
                i=code[ip+1]>>4;
                type1=((code[ip+1]>>2)&3);	type2=(code[ip+1]&3);
                data1=code+ip+2;			data2=data1+4;
                if (type1==0) data1=(vbyte*)&(regs[*data1]);
                else if (type1==1) data1=code+regs[*data1];
                else if (type1==2) break;
                else if (type1==3) data1=code+*(vdword*)data1;
                if (type2==0) data2=(vbyte*)&(regs[*data2]);
                else if (type2==1) data2=code+regs[*data2];
                else if (type2==3) data2=code+*(vdword*)data2;
                if (mode==0) {
                    if (i==0) *(vdword*)data1+=*(vdword*)data2;	else
                    if (i==1) *(vdword*)data1-=*(vdword*)data2;	else
                    if (i==2) *(vdword*)data1*=*(vdword*)data2; else {
                        if (*(vdword*)data2==0) break;
                        if (i==3) *(vdword*)data1/=*(vdword*)data2; else
                        if (i==4) *(vdword*)data1%=*(vdword*)data2;
                    }
                } else if (mode==1) {
                    if (i==0) *(vword*)data1+=*(vword*)data2; else
                    if (i==1) *(vword*)data1-=*(vword*)data2; else
                    if (i==2) *(vword*)data1*=*(vword*)data2; else {
                        if (*(vword*)data2==0) break;
                        if (i==3) *(vword*)data1/=*(vword*)data2;
                        if (i==4) *(vword*)data1%=*(vword*)data2;
                    }
                } else if (mode==2) {
                    if (i==0) *(vbyte*)data1+=*(vbyte*)data2; else
                    if (i==1) *(vbyte*)data1-=*(vbyte*)data2; else
                    if (i==2) *(vbyte*)data1*=*(vbyte*)data2; else {
                        if (*(vbyte*)data2==0) break;
                        if (i==3) *(vbyte*)data1/=*(vbyte*)data2; else
                        if (i==4) *(vbyte*)data1%=*(vbyte*)data2;
                    }
                } else if (mode==3) {
                    if (i==0) *(vfloat*)data1+=*(vfloat*)data2; else
                    if (i==1) *(vfloat*)data1-=*(vfloat*)data2; else
                    if (i==2) *(vfloat*)data1*=*(vfloat*)data2; else {
                        if (*(vfloat*)data2==0) break;
                        if (i==3) *(vfloat*)data1/=*(vfloat*)data2;
                        else break;
                    }
                } else if (mode==4) {
                    if (i==0) *(vint*)data1+=*(vint*)data2; else
                    if (i==1) *(vint*)data1-=*(vint*)data2; else
                    if (i==2) *(vint*)data1*=*(vint*)data2; else {
                        if (*(vint*)data2==0) break;
                        if (i==3) *(vint*)data1/=*(vint*)data2; else
                        if (i==4) *(vint*)data1%=*(vint*)data2;
                    }
                }
                break;
            case 0xF0:
                oprlen=1;
                VmStatus=1;
                break;
            default:
                break;
        }
        if (VmRegs.RP==ip) VmRegs.RP+=oprlen;
    }

    memcpy(VmStartup,&VmRegs,sizeof(VmRegs));
    env->ReleaseIntArrayElements(jRegs,(jint *)VmStartup,JNI_COMMIT);
    env->ReleaseByteArrayElements(jexecFileBytes,(jbyte *)VirtualMem,JNI_COMMIT);
    return ip;
}

char *VmIOString(int id)
{
    char *text;
    VmStrings->Error=0;
    if (id==0) return NULL;
    if (id<0) {
        text=(*VmStrings)[-id]->GetString();
        if (VmStrings->Error) return NULL;
    }	else {
        if (id>(int)VmSize) return NULL;
        text=(char*)VirtualMem+id;
    }
    return text;
}

void VmOut(vdword port,vdword value)
{
    long i=VmRegs.R3;
    long j=VmRegs.R2;
    int ivalue=*(int*)&value;
    static char line[256];
    char *text;
    switch (port)
    {
        case 0x00:{
            jenv->CallVoidMethod(jthiz, intprint, value) ;
            break;
        }
        case 0x01:
        case 0x02: {
            if (ivalue>0) text=(char*)VirtualMem+value;
            else if (ivalue<0) text=(*VmStrings)[-ivalue]->GetString();
            else break;
            jsize len = (jsize)strlen(text);
            jbyteArray barr = jenv->NewByteArray(len);
            jenv->SetByteArrayRegion(barr,0,len,(jbyte*)text);
            jenv->CallVoidMethod(jthiz,strprint,barr);
            jenv->DeleteLocalRef(barr);
            break;
        }
        case 0x03:jenv->CallVoidMethod(jthiz,intprint,value); break;
        case 0x04:{
            jenv->CallVoidMethod(jthiz,out4,ivalue);
            break;
        }
        case 0x05: {
            jenv->CallVoidMethod(jthiz,floatprint,(jfloat)*(float*)&value);
            break;
        }
        case 0x0A: {
            jbyteArray jbytes = (jbyteArray)jenv->CallObjectMethod(jthiz,input_int);
            char *bytes = (char *)jenv->GetByteArrayElements(jbytes,0);
            jenv->ReleaseByteArrayElements(jbytes,(jbyte *)bytes,JNI_COMMIT);
            sscanf(bytes,"%ld",&VmRegs.R3);
            break;
        }
        case 0x0B: {
            jbyteArray jbytes = (jbyteArray)jenv->CallObjectMethod(jthiz,input_str);
            char *bytes = (char *)jenv->GetByteArrayElements(jbytes,0);
            jenv->ReleaseByteArrayElements(jbytes,(jbyte *)bytes,JNI_COMMIT);
            (*VmStrings)[-(long)VmRegs.R3]->Set(bytes);
            break;
        }
        case 0x0C: {
            jbyteArray jbytes = (jbyteArray)jenv->CallObjectMethod(jthiz,input_float);
            char *bytes = (char *)jenv->GetByteArrayElements(jbytes,0);
            jenv->ReleaseByteArrayElements(jbytes,(jbyte *)bytes,JNI_COMMIT);
            sscanf(bytes,"%f",&VmRegs.R3);
            break;
        }
        case 0x0d: {
            VmRegs.R3=*(vint*)(VirtualMem+Data_pointor);
            if(Data_pointor+4<VmSize) Data_pointor+=4;
            break;
        }
        case 0x0e:{
            text=VmIOString(Data_pointor); if (!text) break;(*VmStrings)[-VmRegs.R3]->Set(text);
            while(*(vbyte*)(VirtualMem+Data_pointor++)!=0);
            break;
        }
        case 0x0f:{
            VmRegs.R3=*(vdword*)(VirtualMem+Data_pointor);
            if(Data_pointor+4<VmSize) Data_pointor+=4;
            break;
        }
        case 16: jenv->CallVoidMethod(jthiz,setlcd,j,i);break;
        case 17: VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,createpage);break;
        case 18: VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,deletepage,i);break;
        case 19:{
            if (VmRegs.R3<0) text=(*VmStrings)[-VmRegs.R3]->GetString();
            else break;
            jsize len = (jsize)strlen(text);
            jbyteArray barr = jenv->NewByteArray(len);
            jenv->SetByteArrayRegion(barr,0,len,(jbyte*)text);
            VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,loadres,barr,VmRegs.R2);
            jenv->DeleteLocalRef(barr);
            break;
        }
        case 20:{
            jenv->CallVoidMethod(jthiz,showpic,*(vint*)(VirtualMem+i+32),*(vint*)(VirtualMem+i+28),*(vint*)(VirtualMem+i+24),*(vint*)(VirtualMem+i+20),*(vint*)(VirtualMem+i+16),*(vint*)(VirtualMem+i+12),*(vint*)(VirtualMem+i+8),*(vint*)(VirtualMem+i+4),*(vint*)(VirtualMem+i));
            break;
        }
        case 21:{
            jenv->CallVoidMethod(jthiz,flippage,i);
            break;
        }
        case 22: jenv->CallVoidMethod(jthiz,bitbltpage,j,i);break;
        case 23: jenv->CallVoidMethod(jthiz,fillpage,*(vdword*)(VirtualMem+i+20),*(vdword*)(VirtualMem+i+16),*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 24: jenv->CallVoidMethod(jthiz,pixel,*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 25: VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,readpixel,*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 26: jenv->CallVoidMethod(jthiz,freeres,i);break;
        case 27:{
            clock_t currenttime = clock();
            while(clock()<currenttime+VmRegs.R3*tick);
            break;
        }
        case 32: srand(VmRegs.R3+time(0));break;
        case 33: VmRegs.R3=rand()%VmRegs.R3;break;
        case 34: VmRegs.R3=(vint)jenv->CallIntMethod(jthiz,KeyPress,i); break;
        case 35: jenv->CallVoidMethod(jthiz,cls);break;
        case 36: jenv->CallVoidMethod(jthiz,locate,j,i);break;
        case 37: jenv->CallVoidMethod(jthiz,color,*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 38: {
            jenv->CallVoidMethod(jthiz,font,i);
            break;
        }
        case 39: {
            VmRegs.R3 = jenv->CallIntMethod(jthiz,waitkey);
            break;
        }
        case 40: VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,getpicwid,i);break;
        case 41: VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,getpichgt,i);break;
        case 42: jenv->CallVoidMethod(jthiz,pixlocate,j,i);break;
        case 43: {
            //jenv->CallVoidMethod(jthiz,stretchbltpage,*(vint*)(VirtualMem+i+12),*(vint*)(VirtualMem+i+8),*(vint*)(VirtualMem+i+4),*(vint*)(VirtualMem+i));
            jenv->CallVoidMethod(jthiz,stretchbltpage,*(vdword*)(VirtualMem+i+28),*(vdword*)(VirtualMem+i+24),*(vdword*)(VirtualMem+i+20),*(vdword*)(VirtualMem+i+16),*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));
            break;
        }
        case 44: jenv->CallVoidMethod(jthiz,setbkmode,i);break;
        case 45: {
            if(i<0){
                line[0]=jenv->CallIntMethod(jthiz,inkey);line[1]=0;
                (*VmStrings)[-(long)VmRegs.R3]->Set(line);
            }
            break;
        }
        case 46: VmRegs.R3 = (vint)jenv->CallIntMethod(jthiz,inkey); break;
        case 48: {
            if (VmRegs.R3<0) text=(*VmStrings)[-VmRegs.R3]->GetString();
            else break;
            jsize len = (jsize)strlen(text);
            jbyteArray barr = jenv->NewByteArray(len);
            jenv->SetByteArrayRegion(barr,0,len,(jbyte*)text);
            jenv->CallVoidMethod(jthiz,openfile,barr,VmRegs.R1);
            jenv->DeleteLocalRef(barr);
            break;
        }
        case 49: jenv->CallVoidMethod(jthiz,closefile,ivalue);break;
        case 50: {
            if(ivalue==16){
                VmRegs.R3 = jenv->CallIntMethod(jthiz,getInt,VmRegs.R1);
            }else if(ivalue==17){
                jfloat f = jenv->CallFloatMethod(jthiz,getFloat,VmRegs.R1);
                VmRegs.R3 = *(long*)&f;
            }else if(ivalue==18){
                jbyteArray jbytes = (jbyteArray)jenv->CallObjectMethod(jthiz,getBytes,VmRegs.R1);
                char *bytes = (char *)jenv->GetByteArrayElements(jbytes,0);
                jenv->ReleaseByteArrayElements(jbytes,(jbyte *)bytes,JNI_COMMIT);
                (*VmStrings)[-(long)VmRegs.R3]->Set(bytes);
            }
            break;
        }
        case 51: {
            if(ivalue==16){
                jenv->CallVoidMethod(jthiz,putInt,VmRegs.R1,i);
            }else if(ivalue==17){
                jenv->CallVoidMethod(jthiz,putFloat,VmRegs.R1,*(float*)&i);
            }else if(ivalue==18){
                if (VmRegs.R3<0) text=(*VmStrings)[-VmRegs.R3]->GetString();
                else break;
                jsize len = (jsize)strlen(text);
                jbyteArray barr = jenv->NewByteArray(len);
                jenv->SetByteArrayRegion(barr,0,len,(jbyte*)text);
                jenv->CallVoidMethod(jthiz,putBytes,VmRegs.R1,barr);
                jenv->DeleteLocalRef(barr);
            }
            break;
        }
        case 52: VmRegs.R3 = jenv->CallIntMethod(jthiz,eof,i);break;
        case 53: VmRegs.R3 = jenv->CallIntMethod(jthiz,lof,i);break;
        case 54: VmRegs.R3 = jenv->CallIntMethod(jthiz,loc,i);break;
        case 55: {
            if(ivalue==16){
                jenv->CallVoidMethod(jthiz,seek,VmRegs.R2,i);
            }
            break;
        }
        case 64: jenv->CallVoidMethod(jthiz,setpen,*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 65: jenv->CallVoidMethod(jthiz,setbrush,j,i);break;
        case 66: jenv->CallVoidMethod(jthiz,moveto,VmRegs.R1,j,i);break;
        case 67: jenv->CallVoidMethod(jthiz,lineto,VmRegs.R1,j,i);break;
        case 68: jenv->CallVoidMethod(jthiz,rectangle,*(vdword*)(VirtualMem+i+16),*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 69: jenv->CallVoidMethod(jthiz,circle,*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));break;
        case 80: jenv->CallVoidMethod(jthiz,stretchbltpageEx,*(vdword*)(VirtualMem+i+28),*(vdword*)(VirtualMem+i+24),*(vdword*)(VirtualMem+i+20),*(vdword*)(VirtualMem+i+16),*(vdword*)(VirtualMem+i+12),*(vdword*)(VirtualMem+i+8),*(vdword*)(VirtualMem+i+4),*(vdword*)(VirtualMem+i));
            break;
        default:break;
    }
}

vdword VmIn(vdword port)
{
    long i=VmRegs.R3;
    long j=VmRegs.R2;
    float f;
    char *text,*textp;
    int temp1=0,temp2=0;
    switch (port)
    {
        case 0: i=(int)(*(float*)&VmRegs.R3); break;
        case 1: f=(float)i; i=*(long*)&f; break;
        case 2: i=VmStrings->Alloc()*(-1);(*VmStrings)[-i]->Set("");break;
        case 3: text=VmIOString(i); if (!text) return VmRegs.RP;
            sscanf(text,"%ld",&i);break;
        case 4: if (j>=0) return VmRegs.RP;
            (*VmStrings)[-j]->VarArg("%ld",(long)VmRegs.R3);break;
        case 5: text=VmIOString(j); if (i>=0||!text) return VmRegs.RP;
            (*VmStrings)[-i]->Set(text); break;
        case 6: text=VmIOString(j); if (i>=0||!text) return VmRegs.RP;
            (*VmStrings)[-i]->Append(text); break;
        case 7: text=VmIOString(i); if (!text) return VmRegs.RP;
            i=strlen(text);break;
        case 8: VmStrings->Release(-i); break;
        case 9: text=VmIOString(i); textp=VmIOString(j);
            if (text==NULL||textp==NULL) { i=-1; break; }
            i=strcmp(text,textp); break;
        case 0x0a: if (j>=0) return VmRegs.RP;
            (*VmStrings)[-j]->VarArg("%f",(float)VmRegs.R3);
            break;
        case 0x0b: text=VmIOString(i); if (!text) return VmRegs.RP;
            sscanf(text,"%f",&i); break;
        case 0x0c: text=VmIOString(i); if (text==NULL) { i=-1; break; }
            i=text[j]; break;
        case 0x0d: text=VmIOString(i); if (text==NULL) { i=-1; break; }
            //VmRegs.R1 = (VmRegs.R1 >> 24) & 0x000000ff;
            VmRegs.R1 = VmRegs.R1 & 0x000000ff;
            text[0]=VmRegs.R1;
            text[1]=0;
            (*VmStrings)[-i]->Set(text);
            break;
        case 0x0e: i=65536; break;
        case 0x0f: i=jenv->CallIntMethod(jthiz,gettick); break;
        case 0x10: f=*(float*)&i; f=(float)sin(f);  i=*(long*)&f; break;
        case 0x11: f=*(float*)&i; f=(float)cos(f);  i=*(long*)&f; break;
        case 0x12: f=*(float*)&i; f=(float)tan(f);  i=*(long*)&f; break;
        case 0x13: f=*(float*)&i; f=(float)sqrt(f); i=*(long*)&f; break;
        case 0x14: i=((i>=0)?(i):(-i)); break;
        case 0x15: f=*(float*)&i; f=((f>=0)?(f):(-f)); i=*(long*)&f; break;
        case 0x16: Data_pointor=j;break;
        case 0x17: break;
        case 0x18: break;
        case 0x19: f=9388.0;i=*(long*)&f;break;
        case 32: if (i>=0) return VmRegs.RP;
            (*VmStrings)[-i]->VarArg("%ld",(long)VmRegs.R1);break;
        case 33: text=VmIOString(i); if (!text) return VmRegs.RP;
            sscanf(text,"%ld",&i);break;
        case 34: {
            text=VmIOString(i);
            if (text==NULL) { i=-1; break; }
            if(text[0]>127){
                return text[0]-256;
            }else{
                return text[0];
            }
            //return (int)text[0];
        }
        case 35: text=VmIOString(j);if (VmRegs.R1<1) break;
            text[VmRegs.R1]=0;(*VmStrings)[-i]->Set(text);
            break;
        case 36: text=VmIOString(j);
            while(text[temp1]!=0){temp1++;}
            temp1-=VmRegs.R1;
            if (temp1<0) temp1=0;
            for(temp2=0;text[temp1]!=0;temp2++,temp1++)
                text[temp2]=text[temp1];
            text[temp2]=0;
            (*VmStrings)[-i]->Set(text);
            break;
        case 37: {
            text=VmIOString(j);
            int text_len = strlen(text);

            int startLoc = VmRegs.R1 < text_len ? VmRegs.R1 : 0;
            int cutLen = VmRegs.R0 < text_len ? VmRegs.R0 : text_len;

            startLoc = startLoc > 0 ? startLoc : 0;
            cutLen = cutLen > 0 ? cutLen : 0;

            char *target_text = new char[cutLen+1];
            memset(target_text,0,cutLen+1);

            int loop = 0;
            while(startLoc < text_len && loop < cutLen){
                target_text[loop] = text[startLoc];
                loop++;
                startLoc++;
            }

            (*VmStrings)[-i]->Set(target_text);
            delete target_text;
            break;
        }
        case 38:{
            text=VmIOString(j);
            textp=VmIOString(i);
            i=-1;
            int child_len = strlen(text);
            int parent_len = strlen(textp);
            if (child_len > parent_len) break;
            int endLoc = parent_len - child_len;
            for (int startLoc = 0; startLoc <= endLoc; ++startLoc)
            {
                i = startLoc;
                for(int loop=0;loop<child_len;loop++){
                    if (text[loop] != textp[startLoc+loop])
                    {
                        i = -1;
                        break;
                    }
                }
                if (i != -1) break;
            }
            break;
        }
        case 39: text=VmIOString(i);i=strlen(text);break;
        case 40:{
            VmRegs.R2 = VmRegs.R2 > 31 ? 31 : VmRegs.R2;
            VmRegs.R2 = VmRegs.R2 > 0 ? VmRegs.R2 : 0;
            i = VmRegs.R3 << VmRegs.R2;
            break;
        }
        case 41:{
            VmRegs.R2 = VmRegs.R2 > 31 ? 31 : VmRegs.R2;
            VmRegs.R2 = VmRegs.R2 > 0 ? VmRegs.R2 : 0;
            i = VmRegs.R3 >> VmRegs.R2;
            break;
        }
        case 42:{
            i = ~VmRegs.R3;
            break;
        }
        case 43:{
            i = VmRegs.R3 & VmRegs.R2;
            break;
        }
        case 44:{
            i = VmRegs.R3 || VmRegs.R2;
            break;
        }
        case 45:{
            i = VmRegs.R3 ^ VmRegs.R2;
            break;
        }
        default: break;
    }
    return i;
}