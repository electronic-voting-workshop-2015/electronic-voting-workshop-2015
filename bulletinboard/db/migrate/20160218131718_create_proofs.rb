class CreateProofs < ActiveRecord::Migration
  def change
      create_table :proofs do |t|
          t.binary  :content 
      end
  end
end
