import { Directive, ElementRef, forwardRef, HostListener } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: 'input[appCurrencyInput]',
  standalone: true,
  providers: [{provide:NG_VALUE_ACCESSOR,useExisting:forwardRef(()=>CurrencyInputDirective),multi:true}]
})
export class CurrencyInputDirective implements ControlValueAccessor {
  private change:(value:number|string|'')=>void=()=>{};
  private touched=()=>{};
  constructor(private element:ElementRef<HTMLInputElement>){}
  writeValue(value:number|string|''){
    if(value===''||value==null){
      this.element.nativeElement.value='';
      return;
    }
    this.element.nativeElement.value=typeof value==='string'
      ? this.formatDecimal(value)
      : this.format(Number(value));
  }
  registerOnChange(fn:(value:number|string|'')=>void){this.change=fn;}
  registerOnTouched(fn:()=>void){this.touched=fn;}
  setDisabledState(disabled:boolean){this.element.nativeElement.disabled=disabled;}
  @HostListener('input',['$event.target.value'])
  onInput(raw:string){
    const digits=raw.replace(/\D/g,'');
    if(!digits){this.element.nativeElement.value='';this.change('');return;}
    const decimal=this.decimalFromDigits(digits);
    this.element.nativeElement.value=this.formatDecimal(decimal);
    this.change(Number(decimal)>Number.MAX_SAFE_INTEGER?decimal:Number(decimal));
  }
  @HostListener('blur') onBlur(){this.touched();}
  private format(value:number){return value.toLocaleString('pt-BR',{style:'currency',currency:'BRL'});}
  private decimalFromDigits(digits:string){
    const padded=digits.padStart(3,'0');
    const integer=padded.slice(0,-2).replace(/^0+(?=\d)/,'');
    return `${integer}.${padded.slice(-2)}`;
  }
  private formatDecimal(value:string){
    const normalized=value.replace(',','.');
    const [rawInteger='0',rawFraction=''] = normalized.split('.');
    const integer=rawInteger.replace(/\D/g,'').replace(/^0+(?=\d)/,'')||'0';
    const fraction=(rawFraction.replace(/\D/g,'')+'00').slice(0,2);
    return `R$ ${integer.replace(/\B(?=(\d{3})+(?!\d))/g,'.')},${fraction}`;
  }
}
