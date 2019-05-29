FLAGS=-O1 
# list .s source files here
SRCFILES=main.c

all:	intcalc

intcalc: $(SRCFILES) 
	gcc $(FLAGS) -o intcalc $^ 

clean: 
	rm -f *.o intcalc

