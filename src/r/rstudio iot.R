library(readr)
north <- read_csv("C:/Users/Alex/Desktop/UNI/4rt/north.csv")
#View(north)


north6<-north[as.numeric(north$"Hora") %in% c(3600,25200,46800, 68400) ,]
north6Manaus<-north6[north6$station == "MANAUS",]

df<-north6Manaus[as.numeric(north6Manaus$"Data") >= as.numeric(north6Manaus$"Data"[30185]),]

#View(df)


for(i in 1:length(df$index)){
  a= as.numeric(((df$`TEMPERATURA MÍNIMA NA HORA ANT. (AUT) (°C)`[i] 
                             + df$`TEMPERATURA MÁXIMA NA HORA ANT. (AUT) (°C)`[i])/2))
                             
  #+ rnorm(1,0,0.1)
  df$region[i] = round(a, digits = 2)
}


df$region<-as.numeric(df$region)
df$region


df_out<-df$region

write.csv(df_out,"C:/Users/Alex/Desktop/UNI/4rt/df_out.csv", row.names = FALSE)
write.csv(df,"C:/Users/Alex/Desktop/UNI/4rt/df.csv", row.names = FALSE)




#####################################

df_ts<-ts(df$region[1:460],start=c(1,1),frequency=4)
df_ts


plot.ts(df_ts)
plot(decompose(df_ts))




hw.ts=HoltWinters(df_ts, alpha = NULL, beta = NULL, gamma = NULL,seasonal = "additive")

p.ts=predict(hw.ts, n.ahead = 4, prediction.interval = TRUE,level = 0.95)
plot(hw.ts,p.ts)

p.ts


#######################################



df_ts_short<-ts(df$region[421:460],start=c(1,1),frequency=4)
df_ts_short



plot.ts(df_ts_short)
plot(decompose(df_ts_short))




hw.ts_short=HoltWinters(df_ts_short, alpha = NULL, beta = NULL, gamma = NULL,seasonal = "additive")

p.ts_short=predict(hw.ts_short, n.ahead = 4, prediction.interval = TRUE,level = 0.95)
plot(hw.ts_short,p.ts_short)


p.ts
p.ts_short
df$region[461:464]


####################################

decom<-decompose(df_ts)


T=time(df_ts)
T

t= 1:length(T)
t

df_ts.lm = lm(df_ts~t)

beta=df_ts.lm$coefficients
beta

plot(t,df_ts)
lines(t,df_ts, type = "l")
lines(t,beta[1]+beta[2]*t + decom$seasonal[t],col="blue",lwd=2)
lines(t,beta[1]+beta[2]*t,col="red",lwd=2)


T1 = beta[1]+beta[2]*(length(T)+1) +  decom$seasonal[1]
T2 = beta[1]+beta[2]*(length(T)+2) +  decom$seasonal[2]
T3 = beta[1]+beta[2]*(length(T)+3) +  decom$seasonal[3]
T4 = beta[1]+beta[2]*(length(T)+4) +  decom$seasonal[4]
T1
T2
T3
T4
