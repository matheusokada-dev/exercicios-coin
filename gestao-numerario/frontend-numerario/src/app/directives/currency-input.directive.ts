import { Directive, ElementRef, forwardRef, HostListener } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: 'input[appCurrencyInput]',
  standalone: true,
  providers: [{provide:NG_VALUE_ACCESSOR,useExisting:forwardRef(()=>CurrencyInputDirective),multi:true}]
})
export class CurrencyInputDirective implements ControlValueAccessor {
  private change:(value:number|'')=>void=()=>{};
  private touched=()=>{};
  constructor(private element:ElementRef<HTMLInputElement>){}
  writeValue(value:number|''){
    this.element.nativeElement.value=value===''||value==null?'':this.format(Number(value));
  }
  registerOnChange(fn:(value:number|'')=>void){this.change=fn;}
  registerOnTouched(fn:()=>void){this.touched=fn;}
  setDisabledState(disabled:boolean){this.element.nativeElement.disabled=disabled;}
  @HostListener('input',['$event.target.value'])
  onInput(raw:string){
    const digits=raw.replace(/\D/g,'');
    if(!digits){this.element.nativeElement.value='';this.change('');return;}
    const value=Number(digits)/100;
    this.element.nativeElement.value=this.format(value);
    this.change(value);
  }
  @HostListener('blur') onBlur(){this.touched();}
  private format(value:number){return value.toLocaleString('pt-BR',{style:'currency',currency:'BRL'});}
}
